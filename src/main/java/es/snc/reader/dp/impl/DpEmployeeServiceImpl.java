package es.snc.reader.dp.impl;

import java.util.ArrayList;
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
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import es.snc.common.persistence.PaginatedList;
import es.snc.dp.dto.CompanyDto;
import es.snc.dp.dto.EmployeeDto;
import es.snc.dp.dto.RequestHolidayDto;
import es.snc.reader.dp.IDpEmployeeService;
import es.snc.reader.dto.FilterEmployee;
import es.snc.reader.dto.FilterRequestHoliday;

@Service
@PropertySource({ "classpath:application.properties" })
public class DpEmployeeServiceImpl implements IDpEmployeeService{

	private @Value("${dp.api.url}") String dpApiUrl;
	private @Value("${client.id}") String clientId;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DpEmployeeServiceImpl.class);
	private static final String DEFAULT_CLIENTS = "clients";
	private static final String DEFAULT_EMPLOYEES = "employees";
	private static final String DEFAULT_SEARCH_ONE = "searchOne";
	private static final String DEFAULT_AUTH_BEARER = "Bearer ";
	private static final String DEFAULT_AUTH_HEADER_NAME = "Authorization";

	private final RestTemplate restTemplate;

	@Autowired
	public DpEmployeeServiceImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	
	@Override
	public List<EmployeeDto> searchAll(String token) {

		List<EmployeeDto> results = new ArrayList<>();

		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(DEFAULT_AUTH_HEADER_NAME, DEFAULT_AUTH_BEARER + token);

			UriComponentsBuilder uriBuilder = UriComponentsBuilder
					.fromHttpUrl(dpApiUrl)
					.pathSegment(DEFAULT_CLIENTS)
					.pathSegment(clientId)
					.pathSegment(DEFAULT_EMPLOYEES);

			LOGGER.debug(uriBuilder.toUriString());

			ResponseEntity<List<EmployeeDto>> response = restTemplate.exchange(
					uriBuilder.toUriString(),
					HttpMethod.GET, 
					new HttpEntity<>(headers), 
					new ParameterizedTypeReference<List<EmployeeDto>>() {
					});

			if (response.getStatusCode().equals(HttpStatus.OK)) {
				results = response.getBody();
			}

		} catch (Exception e) {

			LOGGER.error("Failed to connect with Digital People API. DpEmployeeService searchAll. ");
		}

		return results;
	}


	@Override
	public EmployeeDto searchById(String token, Long id) {
		EmployeeDto result = null;
		
		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(DEFAULT_AUTH_HEADER_NAME, DEFAULT_AUTH_BEARER + token);

			UriComponentsBuilder uriBuilder = UriComponentsBuilder
					.fromHttpUrl(dpApiUrl)
					.pathSegment(DEFAULT_CLIENTS)
					.pathSegment(clientId)
					.pathSegment(DEFAULT_EMPLOYEES)
					.pathSegment(id.toString());

			LOGGER.debug(uriBuilder.toUriString());

			ResponseEntity<EmployeeDto> response = restTemplate.exchange(
					uriBuilder.toUriString(),
					HttpMethod.GET, 
					new HttpEntity<>(headers), 
					new ParameterizedTypeReference<EmployeeDto>() {
					});

			if (response.getStatusCode().equals(HttpStatus.OK)) {
				result = response.getBody();
			}

		} catch (Exception e) {
			LOGGER.error("Failed to connect with Digital People API. DpEmployeeService searchById. ");
		}
		
		return result;
		
	}


	@Override
	public EmployeeDto searchByCardId(String token, FilterEmployee filterEmployee) {
		EmployeeDto result = null;
		
		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(DEFAULT_AUTH_HEADER_NAME, DEFAULT_AUTH_BEARER + token);
			
			
			UriComponentsBuilder uriBuilder = UriComponentsBuilder
					.fromHttpUrl(dpApiUrl)
					.pathSegment(DEFAULT_CLIENTS)
					.pathSegment(clientId)
					.pathSegment(DEFAULT_EMPLOYEES)
					.pathSegment(DEFAULT_SEARCH_ONE)
					.queryParam("firstResult", true);

			LOGGER.debug(uriBuilder.toUriString());

			ResponseEntity <EmployeeDto> response = restTemplate.exchange(
					uriBuilder.toUriString(),
					HttpMethod.POST, 
					new HttpEntity<FilterEmployee>(filterEmployee, headers), 
					new ParameterizedTypeReference<EmployeeDto>() {
					});

			if (response.getStatusCode().equals(HttpStatus.OK)) {
				result = response.getBody();
			}

		} catch (Exception e) {

			LOGGER.error("Failed to connect with Digital People API. DpEmployeeService searchByCardId. ");
		}
		
		return result;
	}

}
