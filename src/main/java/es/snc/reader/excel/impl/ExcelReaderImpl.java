package es.snc.reader.excel.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import es.snc.dp.dto.RequestHolidayDto;
import es.snc.reader.dpverification.DpVerification;
import es.snc.reader.dto.FilterUserDto;
import es.snc.reader.excel.IExcelReader;
import es.snc.reader.security.ISecurityUserService;
import es.snc.reader.security.dto.JwtTokenDto;
import es.snc.reader.utils.ExcelInfo;
import es.snc.reader.utils.FindSetters;
import es.snc.security.dto.UserDto;
import es.snc.security.filter.UserFilter;

@Service
@PropertySource({ "classpath:application.properties" })
public class ExcelReaderImpl implements IExcelReader {

	private @Value("${client.id}") String clientId;
	private @Value("${excelAbsolutePath}") String fileAbsolutePath;

	private final int MAX_EMPTY_ROWS = 50;
	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelReaderImpl.class);

	private JwtTokenDto token;

	@Autowired
	private ISecurityUserService userService;

	@Autowired
	private DpVerification verification;

	@Autowired
	private FindSetters findSetters;

	private List<RequestHolidayDto> list;

	private boolean userReaded;
	private boolean holidaySheetWithoutMistakes;

	@Override
	public void readExcelSheets(JwtTokenDto token) throws FileNotFoundException, IOException {

		this.token = token;

		FileInputStream fis = new FileInputStream(new File(fileAbsolutePath));

		XSSFWorkbook wb = new XSSFWorkbook(fis);

		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			XSSFSheet sheet = wb.getSheetAt(i);

			switch (sheet.getSheetName()) {

			case "Vacaciones":
				readHolidayRequests(sheet);
				break;

			case "Configuración":
				readUsername(sheet);
				break;

			default:
				LOGGER.error("Unknown sheet: " + sheet.getSheetName());
				break;

			}

		}

		if (userReaded && holidaySheetWithoutMistakes)
			verification.RequestHolidayVerification(token, list);
		else
			LOGGER.error("Wrong data, cannot upload excel");

		wb.close();

		fis.close();

	}

	@Override
	public void readUsername(XSSFSheet sheet) {
		userReaded = false;
		
		if (sheet.getLastRowNum()==0) {
			LOGGER.error("No valid username found in the excell");
			return;
		}
		
		
		String username="";
		
		if (sheet.getRow(1).getCell(0).getCellType()==CellType.STRING)
			username= sheet.getRow(1).getCell(0).getStringCellValue();
		else if (sheet.getRow(1).getCell(0).getCellType()==CellType.NUMERIC)
			username= String.valueOf(sheet.getRow(1).getCell(0).getNumericCellValue());
		
		
		if (StringUtils.isBlank(username)) {
			LOGGER.error("No valid username found in the excell");
			return;
		}

		FilterUserDto filter = new FilterUserDto();
		filter.setContent(new UserFilter());
		filter.getContent().setUsername(username);

		try {

			UserDto user = userService.findOneByFilter(filter, token.getToken());

			for (RequestHolidayDto rh : list) {
				rh.setCreatedBy(user.getUserId());
			}
			userReaded = true;

		} catch (Exception e) {
			LOGGER.error("No valid username found in the excell");
			return;
		}

	}

	@Override
	public void readHolidayRequests(XSSFSheet sheet) {

		holidaySheetWithoutMistakes = true;
		List<XSSFCell> firstRow = ExcelInfo.getFirstRow(sheet);
		int cells = ExcelInfo.getNumberOfColumns(sheet);

		list = new ArrayList<>();

		int emptyAcumulated = 0;
		int line = 0;

		Iterator<Row> rowIterator = sheet.iterator();

		while (rowIterator.hasNext()) {
			XSSFRow row = (XSSFRow) rowIterator.next();

			if (line > 0) {
				
				if (row.getCell(0)==null && row.getCell(1)==null && row.getCell(2)==null && row.getCell(3)==null 
						&& row.getCell(4)==null && row.getCell(5)==null && row.getCell(6)==null && row.getCell(7)==null) {
					break;
				}
				
				RequestHolidayDto dto = new RequestHolidayDto();

				dto.setClientId(Long.valueOf(clientId));

				for (int i = 0; i < cells; i++) {

					XSSFCell cell = row.getCell(i);

					dto = findSetters.requestHolidayAuxDtoSetters(dto, cell, firstRow.get(i), token, line + 1);

				}

				if (dto.getEmployeeId() == null || dto.getYear() == null
						|| StringUtils.isBlank(dto.getFromDate()) || StringUtils.isBlank(dto.getUntilDate())
						|| dto.getRequestedTime() == null || dto.getCompanyId() == null) {

					emptyAcumulated++;

					if (emptyAcumulated == 1) {
						holidaySheetWithoutMistakes = false;
						LOGGER.error("Error, required fields are empty. Sheet: " + sheet.getSheetName() + ". From row: "
								+ (line + 1));
					}
				} else {

					if (emptyAcumulated != 0) {
						holidaySheetWithoutMistakes = false;
						LOGGER.error("Error, required fields are empty. Sheet: " + sheet.getSheetName() + ". To row: "
								+ line);
					}
					emptyAcumulated = 0;

					boolean duplicated = false;

					for (RequestHolidayDto e : list) {

						if (e.getEmployeeId().equals(dto.getEmployeeId()) && e.getFromDate().equals(dto.getFromDate())
								&& e.getUntilDate().equals(dto.getFromDate())) {
							holidaySheetWithoutMistakes = false;
							duplicated = true;
							LOGGER.info("Required fields cannot be duplicated. Sheet: " + sheet.getSheetName()
									+ " (Error in line " + line + ")");
							break;
						}
					}

					if (!duplicated) {
						list.add(dto);
					}
				}

				if (emptyAcumulated == MAX_EMPTY_ROWS) {
					LOGGER.error("End of sheet: " + sheet.getSheetName() + ". Row: " + ((line + 1) - MAX_EMPTY_ROWS));
					break;
				}
			}
			line++;
		}

		LOGGER.info("Rows readed from sheet " + sheet.getSheetName() + ": " + (line - 1));
	}

}
