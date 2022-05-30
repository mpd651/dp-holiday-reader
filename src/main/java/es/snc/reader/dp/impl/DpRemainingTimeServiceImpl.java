package es.snc.reader.dp.impl;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import es.snc.dp.dto.EmployeeDto;
import es.snc.dp.dto.EmployeeRemainingTimeDto;
import es.snc.dp.dto.RequestHolidayDto;
import es.snc.reader.dp.IDpRemainingTimeService;

@Service
@PropertySource({ "classpath:application.properties" })
public class DpRemainingTimeServiceImpl implements IDpRemainingTimeService {

	
	private @Value("${dp.api.url}") String dpApiUrl;
	private @Value("${client.id}") String clientId;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DpRemainingTimeServiceImpl.class);
	private static final String DEFAULT_CLIENTS = "clients";
	private static final String DEFAULT_REMAINING_TIME = "remainingTime";
	private static final String DEFAULT_AUTH_BEARER = "Bearer ";
	private static final String DEFAULT_AUTH_HEADER_NAME = "Authorization";

	private final RestTemplate restTemplate;

	@Autowired
	public DpRemainingTimeServiceImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@Override
	public EmployeeRemainingTimeDto getEmployeeRemainingTime(String token, RequestHolidayDto dto) {
		EmployeeRemainingTimeDto result = null;
		
		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(DEFAULT_AUTH_HEADER_NAME, DEFAULT_AUTH_BEARER + token);

			UriComponentsBuilder uriBuilder = UriComponentsBuilder
					.fromHttpUrl(dpApiUrl)
					.pathSegment(DEFAULT_CLIENTS)
					.pathSegment(clientId)
					.pathSegment(DEFAULT_REMAINING_TIME)
					.pathSegment(dto.getEmployeeId().toString())
					.pathSegment(dto.getCompanyId().toString())
					.pathSegment(dto.getYear().toString());

			LOGGER.debug(uriBuilder.toUriString());

			ResponseEntity<EmployeeRemainingTimeDto> response = restTemplate.exchange(
					uriBuilder.toUriString(),
					HttpMethod.GET, 
					new HttpEntity<>(headers), 
					new ParameterizedTypeReference<EmployeeRemainingTimeDto>() {
					});

			if (response.getStatusCode().equals(HttpStatus.OK)) {
				result = response.getBody();
			}

		} catch (Exception e) {
			LOGGER.error("Failed to connect with Digital People API. DpRemainingTimeService getEmployeeRemainingTime. ");
		}
		
		return result;
		
	}

}
