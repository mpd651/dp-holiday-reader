package es.snc.reader.dpverification;

import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import es.snc.common.persistence.PaginatedList;
import es.snc.common.persistence.filter.Filter;
import es.snc.common.persistence.filter.FilterWithDate;
import es.snc.dp.dto.CompanyDto;
import es.snc.dp.dto.ContractDto;
import es.snc.dp.dto.EmployeeRemainingTimeDto;
import es.snc.dp.dto.RequestHolidayDto;
import es.snc.dp.filter.ContractFilter;
import es.snc.dp.filter.RequestFilter;
import es.snc.dp.persistence.model.RequestHoliday;
import es.snc.reader.dp.IDpCalendarService;
import es.snc.reader.dp.IDpContractService;
import es.snc.reader.dp.IDpEmployeeService;
import es.snc.reader.dp.IDpRemainingTimeService;
//import es.snc.reader.security.dto.JwtTokenDto;
import es.snc.reader.dp.IDpRequestsHolidayService;
import es.snc.reader.dto.FilterRequestHoliday;
import es.snc.reader.security.dto.JwtTokenDto;
import es.snc.vf.persistence.enumeration.ValidationStatus;

@Service
public class DpVerification {
	private static final Logger LOGGER = LoggerFactory.getLogger(DpVerification.class);

	@Autowired
	private IDpRequestsHolidayService dpRequestsHolidayService;

	@Autowired
	private IDpEmployeeService dpEmployeeService;

	@Autowired
	private IDpRemainingTimeService dpRemainingTimeService;

	@Autowired
	private IDpCalendarService dpCalendarService;

	@Autowired
	private IDpContractService dpContractService;

	private SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private boolean excelCorrect;

	private String token;

	public void RequestHolidayVerification(JwtTokenDto tokenDto, List<RequestHolidayDto> dtosList) {

		token = tokenDto.getToken();
		excelCorrect = true;

		for (RequestHolidayDto dto : dtosList) {

			if (checkContract(dto) == false || checkDates(dto) == true || checkTime(dto) == false) {
				excelCorrect = false;
			}

		}

		if (excelCorrect) {
			for (RequestHolidayDto dto : dtosList) {

				try {

					RequestHolidayDto created = dpRequestsHolidayService.create(token, dto);

				} catch (HttpClientErrorException e) {

					LOGGER.error("Failed to connect with Digital People API. DpRequestsHolidayService create.");

					String error = e.getResponseHeaders().get("ErrorCode").get(0);

					LOGGER.error("Request holiday for employee "
							+ dpEmployeeService.searchById(token, dto.getEmployeeId()).getCardId() + ", from date "
							+ convertUtcStringToDate(dto.getFromDate(), false) + " until date "
							+ convertUtcStringToDate(dto.getUntilDate(), true) + " was not created." + "Error message: "
							+ getErrorMessage(error));

				}

			}
			LOGGER.info("Correct data, excel uploaded");
		} else {
			LOGGER.error("Wrong data, cannot upload excel");
		}

	}

	public boolean checkTime(RequestHolidayDto dto) {

		boolean checkedRequestedTime = true;

		try {
			Long timeBetweenDates = ChronoUnit.MINUTES.between(sourceFormat.parse(dto.getFromDate()).toInstant(),
					sourceFormat.parse(dto.getUntilDate()).toInstant());

			if (dto.getRequestedTime() == 0L) {
				LOGGER.error("Request holiday for employee "
						+ dpEmployeeService.searchById(token, dto.getEmployeeId()).getCardId() + ", from date "
						+ convertUtcStringToDate(dto.getFromDate(), false) + " until date "
						+ convertUtcStringToDate(dto.getUntilDate(), true) + " was not created."
						+ "Requested time cannot be 0.");
				checkedRequestedTime = false;
			}

			if (dto.getRequestedTime() > timeBetweenDates) {
				LOGGER.error("Request holiday for employee "
						+ dpEmployeeService.searchById(token, dto.getEmployeeId()).getCardId() + ", from date "
						+ convertUtcStringToDate(dto.getFromDate(), false) + " until date "
						+ convertUtcStringToDate(dto.getUntilDate(), true) + " was not created."
						+ "Requested time cannot be more than days between dates.");
				checkedRequestedTime = false;
			}

			EmployeeRemainingTimeDto employeeRemainingTimeDto = dpRemainingTimeService.getEmployeeRemainingTime(token,
					dto);

			if (employeeRemainingTimeDto != null
					&& employeeRemainingTimeDto.getHolidayTime() < dto.getRequestedTime() + getDraftPendingTime(dto)) {
				LOGGER.error("Request holiday for employee "
						+ dpEmployeeService.searchById(token, dto.getEmployeeId()).getCardId() + ", from date "
						+ convertUtcStringToDate(dto.getFromDate(), false) + " until date "
						+ convertUtcStringToDate(dto.getUntilDate(), true) + " was not created."
						+ "Not enough remaining time for the holidays.");
				checkedRequestedTime = false;
			}

		} catch (ParseException e) {
		}

		return checkedRequestedTime;

	}

	private long getDraftPendingTime(RequestHolidayDto dto) {

		RequestFilter requestFilter = new RequestFilter();
		requestFilter.setEmployeeId(dto.getEmployeeId());
		requestFilter.setCompanyId(dto.getCompanyId());
		requestFilter.setYear(dto.getYear());
		requestFilter.setStatus(Arrays.asList(ValidationStatus.DRAFT, ValidationStatus.PENDING));

		FilterRequestHoliday filter = new FilterRequestHoliday();
		filter.setContent(requestFilter);

		List<RequestHolidayDto> results = dpRequestsHolidayService.searchByFilter(token, filter).getResults();

		long result = 0L;
		for (RequestHolidayDto rh : results) {

			result += rh.getRequestedTime();
		}

		return result;
	}

	public boolean checkContract(RequestHolidayDto dto) {
		boolean contract = true;

		FilterWithDate filter = new FilterWithDate();
		filter.setFromDate(convertUtcStringToDate(dto.getFromDate(), false));
		filter.setUntilDate(convertUtcStringToDate(dto.getUntilDate(), true));

		ContractFilter content = new ContractFilter();
		content.setEmployeeId(dto.getEmployeeId());
		content.setCompanyId(dto.getCompanyId());
		filter.setContent(content);

		PaginatedList<ContractDto> list = dpContractService.searchContractByFilter(token, filter);

		if (list == null || list.getResults().size() == 0) {
			contract = false;
			LOGGER.error("Request holiday for employee "
					+ dpEmployeeService.searchById(token, dto.getEmployeeId()).getCardId() + ", from date "
					+ convertUtcStringToDate(dto.getFromDate(), false) + " until date "
					+ convertUtcStringToDate(dto.getUntilDate(), true) + " was not created."
					+ "Employee does not have a contract in the holiday dates.");
		}

		return contract;
	}

	public boolean checkDates(RequestHolidayDto dto) {
		boolean overlappingDates = false;

		FilterRequestHoliday filterRequestHoliday = new FilterRequestHoliday();
		filterRequestHoliday.setContent(new RequestFilter());

		filterRequestHoliday.getContent().setEmployeeId(dto.getEmployeeId());
		filterRequestHoliday.getContent().setType(dto.getType());
		filterRequestHoliday.getContent().setYear(dto.getYear());

		PaginatedList<RequestHolidayDto> employeeHolidaysDp = dpRequestsHolidayService.searchByFilter(token,
				filterRequestHoliday);

		for (RequestHolidayDto dpHoliday : employeeHolidaysDp.getResults()) {

			try {
				if (!sourceFormat.parse(dpHoliday.getFromDate()).before(sourceFormat.parse(dto.getFromDate()))
						&& !sourceFormat.parse(dpHoliday.getFromDate()).after(sourceFormat.parse(dto.getUntilDate()))) {
					overlappingDates = true;
				}

				if (!sourceFormat.parse(dpHoliday.getUntilDate()).before(sourceFormat.parse(dto.getFromDate()))
						&& !sourceFormat.parse(dpHoliday.getUntilDate())
								.after(sourceFormat.parse(dto.getUntilDate()))) {
					overlappingDates = true;
				}

			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		if (overlappingDates)
			LOGGER.error("Request holiday for employee "
					+ dpEmployeeService.searchById(token, dto.getEmployeeId()).getCardId() + ", from date "
					+ convertUtcStringToDate(dto.getFromDate(), false) + " until date "
					+ convertUtcStringToDate(dto.getUntilDate(), true) + " was not created." + "Error message: "
					+ getErrorMessage("422_01"));

		return overlappingDates;
	}

	public static String convertUtcStringToDate(String dateStr, boolean until) {
		try {
			SimpleDateFormat destFormat = new SimpleDateFormat("yyyy-MM-dd");
			TimeZone utc = TimeZone.getTimeZone("UTC");
			SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sourceFormat.setTimeZone(utc);
			Date convertedDate = sourceFormat.parse(dateStr);

			if (until)
				convertedDate = DateUtils.addDays(convertedDate, -1);

			return destFormat.format(convertedDate);
		} catch (ParseException e) {
			LOGGER.error("Error parsing dates");
		}
		return null;
	}

	public String getErrorMessage(String errorCode) {
		String errorMessage = "";
		switch (errorCode) {
		case "422_01":
			errorMessage = "UNPROCESSABLE_OVERLAPPING_DATES";
			break;
		case "422_02":
			errorMessage = "UNPROCESSABLE_TOO_MUCH_REQUESTED_TIME";
			break;
		case "422_03":
			errorMessage = "UNPROCESSABLE_ResolvedRequestNotEditable";
			break;
		case "422_04":
			errorMessage = "UNPROCESSABLE_ExpirityDateReached";
			break;
		case "422_05":
			errorMessage = "UNPROCESSABLE_TOO_MUCH_REQUESTED_TIME";
			break;
		case "422_06":
			errorMessage = "UNPROCESSABLE_NO_REQUESTED_TIME";
			break;
		}

		return errorMessage;
	}
}
