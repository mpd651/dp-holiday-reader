package es.snc.reader.dp;

import es.snc.dp.dto.EmployeeRemainingTimeDto;
import es.snc.dp.dto.RequestHolidayDto;

public interface IDpRemainingTimeService {
	
	EmployeeRemainingTimeDto getEmployeeRemainingTime (String token, RequestHolidayDto dto);
}
