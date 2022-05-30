package es.snc.reader.excel;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import es.snc.reader.security.dto.JwtTokenDto;


public interface IExcelReader {

	
	public void readExcelSheets(JwtTokenDto token) throws FileNotFoundException, IOException;
	
	public void readUsername (XSSFSheet sheet);
	
	public void readHolidayRequests(XSSFSheet sheet);
}
