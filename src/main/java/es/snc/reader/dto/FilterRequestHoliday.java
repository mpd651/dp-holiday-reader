package es.snc.reader.dto;

import es.snc.dp.filter.RequestFilter;
import es.snc.dp.persistence.enumeration.RequestType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterRequestHoliday {
	
	public RequestFilter content;
	
}
