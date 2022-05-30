package es.snc.reader.dp;

import java.util.List;

import es.snc.dp.dto.EmployeeDto;
import es.snc.reader.dto.FilterEmployee;

public interface IDpEmployeeService {
	
	
	List<EmployeeDto> searchAll(String token);
	
	EmployeeDto searchById(String token, Long id);
	
	EmployeeDto searchByCardId(String token, FilterEmployee filterEmployee);
}
