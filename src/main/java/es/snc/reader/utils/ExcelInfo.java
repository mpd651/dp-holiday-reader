package es.snc.reader.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class ExcelInfo {

	public static List<XSSFCell> getFirstRow(XSSFSheet sheet) {
		List<XSSFCell> list = new ArrayList<>();
		Row firstRow = sheet.getRow(0);
		Iterator<?> iterator = firstRow.cellIterator();
		while (iterator.hasNext()) {
			XSSFCell cell = (XSSFCell) iterator.next();
			String cellValue = cell.getStringCellValue();
			if (cellValue.equals("")) {
				break;
			} else {
				list.add(cell);
			}
		}

		return list;
	}

	public static int getNumberOfColumns(XSSFSheet sheet) {
		int i = 0;

		Row firstRow = sheet.getRow(0);
		Iterator<?> iterator = firstRow.cellIterator();
		while (iterator.hasNext()) {
			XSSFCell cell = (XSSFCell) iterator.next();
			String cellValue = cell.getStringCellValue();
			if (cellValue.equals("")) {
				break;
			}
			i++;
		}
		// System.out.println("Number of cells: " + i);
		return i;
	}

}
