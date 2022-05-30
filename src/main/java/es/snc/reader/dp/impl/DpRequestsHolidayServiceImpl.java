package es.snc.reader.dp.impl;

import java.net.http.HttpResponse;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import es.snc.common.persistence.PaginatedList;
import es.snc.dp.dto.EmployeeDto;
import es.snc.dp.dto.RequestHolidayDto;
import es.snc.dp.filter.RequestFilter;
import es.snc.reader.dp.IDpRequestsHolidayService;
import es.snc.reader.dto.FilterRequestHoliday;

@Service
@PropertySource({ "classpath:application.properties" })
public class DpRequestsHolidayServiceImpl implements IDpRequestsHolidayService {
	
	private @Value("${dp.api.url}") String dpApiUrl;
	private @Value("${client.id}") String clientId;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DpRequestsHolidayServiceImpl.class);
	private static final String DEFAULT_CLIENTS = "clients";
	private static final String DEFAULT_EMPLOYEES = "employees";
	private static final String DEFAULT_AUTH_BEARER = "Bearer ";
	private static final String DEFAULT_AUTH_HEADER_NAME = "Authorization";
	private static final String DEFAULT_REQUESTS_HOLIDAYS = "requestsHoliday";
	private static final String DEFAULT_SEARCH_ALL="searchAll";

	private final RestTemplate restTemplate;

	@Autowired
	public DpRequestsHolidayServiceImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public RequestHolidayDto create(String token, RequestHolidayDto dto) throws HttpClientErrorException {
		RequestHolidayDto result = null;


			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(DEFAULT_AUTH_HEADER_NAME, DEFAULT_AUTH_BEARER + token);

			UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(dpApiUrl).pathSegment(DEFAULT_CLIENTS)
					.pathSegment(clientId).pathSegment(DEFAULT_REQUESTS_HOLIDAYS).queryParam("autoApprove", true);

			LOGGER.debug(uriBuilder.toUriString());

			ResponseEntity<RequestHolidayDto> response = restTemplate.exchange(uriBuilder.toUriString(),
					HttpMethod.POST, new HttpEntity<RequestHolidayDto>(dto, headers),
					new ParameterizedTypeReference<RequestHolidayDto>() {
					});

			if (response.getStatusCode().equals(HttpStatus.OK)) {
				result = response.getBody();
				LOGGER.info("RequestHoliday added");
			}
		

		return result;
	}

	@Override
	public PaginatedList<RequestHolidayDto> searchByFilter(String token, FilterRequestHoliday requestFilter) {
		PaginatedList <RequestHolidayDto> result = null;
		
		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(DEFAULT_AUTH_HEADER_NAME, DEFAULT_AUTH_BEARER + token);
			
			
			UriComponentsBuilder uriBuilder = UriComponentsBuilder
					.fromHttpUrl(dpApiUrl)
					.pathSegment(DEFAULT_CLIENTS)
					.pathSegment(clientId)
					.pathSegment(DEFAULT_REQUESTS_HOLIDAYS)
					.pathSegment(DEFAULT_SEARCH_ALL);

			LOGGER.debug(uriBuilder.toUriString());

			ResponseEntity <PaginatedList<RequestHolidayDto>> response = restTemplate.exchange(
					uriBuilder.toUriString(),
					HttpMethod.POST, 
					new HttpEntity<FilterRequestHoliday>(requestFilter, headers), 
					new ParameterizedTypeReference<PaginatedList<RequestHolidayDto>>() {
					});

			if (response.getStatusCode().equals(HttpStatus.OK)) {
				result = response.getBody();
			}

		} catch (Exception e) {

			LOGGER.error("Failed to connect with Digital People API. DpRequestHolidayService searchByFilter. ");
		}
		
		return result;
	}

}
