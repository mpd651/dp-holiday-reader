package es.snc.reader.dp;

import java.util.List;

import es.snc.dp.dto.CompanyDto;
import es.snc.dp.dto.EmployeeDto;

public interface IDpCompanyService {

	List<CompanyDto> searchAll(String token);

}
