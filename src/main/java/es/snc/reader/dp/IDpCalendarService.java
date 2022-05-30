package es.snc.reader.dp;

import es.snc.dp.dto.RequestHolidayDto;

public interface IDpCalendarService {
	
	int minutesBetweenDates(String token, RequestHolidayDto dto);
}
