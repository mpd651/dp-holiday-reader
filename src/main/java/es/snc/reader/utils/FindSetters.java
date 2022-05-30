package es.snc.reader.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import es.snc.dp.dto.CompanyDto;
import es.snc.dp.dto.EmployeeDto;
import es.snc.dp.dto.EmployeeTypeDto;
import es.snc.dp.dto.RequestHolidayDto;
import es.snc.dp.filter.EmployeeFilter;
import es.snc.reader.dp.IDpCompanyService;
import es.snc.reader.dp.IDpEmployeeService;
import es.snc.reader.dp.impl.DpEmployeeServiceImpl;
import es.snc.reader.dto.FilterEmployee;
import es.snc.reader.security.dto.JwtTokenDto;
import es.snc.reader.service.impl.ServiceImpl;

@Service
public class FindSetters {

	@Autowired
	private IDpEmployeeService employeeService;

	@Autowired
	private IDpCompanyService companyService;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceImpl.class);

	public static String valueDateOrStringCells(XSSFCell cell, boolean until) {

		String stringValue = null;

		if (cell.getCellType().equals(CellType.NUMERIC)) {

			LocalDateTime date = cell.getLocalDateTimeCellValue();

			if (until)
				date = date.plusDays(1);

			if (date != null) {
				stringValue = date.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toInstant()
						.toString();
			}

		} else if (cell.getCellType().equals(CellType.STRING)) {

			String dateString = cell.getStringCellValue();

			LocalDate date = null;

			if (dateString.contains("-")) {

				date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));

				if (until)
					date = date.plusDays(1);

			} else if (dateString.contains("/")) {

				date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));

				if (until)
					date = date.plusDays(1);
			}

			stringValue = date.atStartOfDay().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"))
					.toInstant().toString();
		}

		return stringValue;
	}

	public RequestHolidayDto requestHolidayAuxDtoSetters(RequestHolidayDto dto, XSSFCell cell, XSSFCell cellFirstRow,
			JwtTokenDto token, int line) {

		if (cell != null) {

			switch (cellFirstRow.getStringCellValue().trim()) {

			case "Código de empresa":

				String code = "";

				if (cell.getCellType().equals(CellType.NUMERIC)) {
					code = String.valueOf((int) cell.getNumericCellValue()).trim();

				} else if (cell.getCellType().equals(CellType.STRING)) {
					code = cell.getStringCellValue().trim();
				}

				Map<String, CompanyDto> companiesMapCodeAsKey = companiesMapCodeAsKey(token.getToken());

				if (companiesMapCodeAsKey.get(code) == null) {
					LOGGER.error("Error. Company with code " + code + " not found in database. Line " + line);
					break;
				}

				dto.setCompanyId(companiesMapCodeAsKey.get(code).getId());
				break;

			case "DNI Empleado":
				if (cell.getCellType()!=CellType.STRING)
					break;
				
				String dni = cell.getStringCellValue().trim();

				FilterEmployee filterEmployee = new FilterEmployee();
				filterEmployee.setContent(new EmployeeFilter());
				filterEmployee.getContent().setCardId(dni);
				EmployeeDto employee = employeeService.searchByCardId(token.getToken(), filterEmployee);

				if (employee == null) {
					LOGGER.error("Error. Employee with IdCard " + dni + " not found in database. Line " + line);
					break;
				}

				dto.setEmployeeId(employee.getId());
				break;

			case "Año del cargo":
				if (cell.getCellType()!=CellType.NUMERIC) {
					LOGGER.error("Error. Need a numeric value in cell 'Año del cargo'. Line "+ line);
					break;
				}
				
				dto.setYear((int) (cell.getNumericCellValue()));
				break;

			case "Fecha desde (inclusiva)":
				dto.setFromDate(valueDateOrStringCells(cell, false));
				break;

			case "Fecha hasta (inclusiva)":
				dto.setUntilDate(valueDateOrStringCells(cell, true));
				break;

			case "Tiempo solicitado (en días)":
				if (cell.getCellType()!=CellType.NUMERIC) {
					LOGGER.error("Error. Need a numeric value in cell 'Tiempo solicitado'. Line "+ line);
					break;
				}

				dto.setRequestedTime((long) (cell.getNumericCellValue() * 24 * 60));
				break;

			case "Justificación (opcional)":
				if (cell.getCellType()!=CellType.STRING) {
					LOGGER.error("Error. Need a String value in cell 'Justificacion'. Line "+ line);
					break;
				}
				
				dto.setJustification(cell.getStringCellValue());
				break;

			case "Comentarios (opcional)":
				if (cell.getCellType()!=CellType.STRING) {
					LOGGER.error("Error. Need a String value in cell 'Comentarios'. Line "+ line);
					break;
				}
					
				dto.setComment(cell.getStringCellValue());
				break;

			}
		}

		return dto;
	}

	public Map<String, CompanyDto> companiesMapCodeAsKey(String token) {

		List<CompanyDto> companiesList = companyService.searchAll(token);
		Map<String, CompanyDto> companies = new HashMap<>();

		boolean repeatedCode = false;

		for (CompanyDto c : companiesList) {

			for (String s : companies.keySet()) {

				if (c.getCode().equals(s)) {

					repeatedCode = true;
				}
			}

			if (!repeatedCode) {

				companies.put(c.getCode(), c);
			}

			repeatedCode = false;
		}

		return companies;
	}
}
