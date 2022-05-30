package es.snc.reader.dp;

import java.util.List;

import es.snc.common.persistence.PaginatedList;
import es.snc.dp.dto.EmployeeDto;
import es.snc.dp.dto.RequestHolidayDto;
import es.snc.dp.filter.RequestFilter;
import es.snc.reader.dto.FilterRequestHoliday;

public interface IDpRequestsHolidayService {

	RequestHolidayDto create(String token, RequestHolidayDto dto);
	
	PaginatedList<RequestHolidayDto> searchByFilter(String token, FilterRequestHoliday requestFilter);

}
