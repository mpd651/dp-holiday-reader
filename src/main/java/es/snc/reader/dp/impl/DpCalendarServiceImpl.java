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

import es.snc.dp.dto.RequestHolidayDto;
import es.snc.reader.dp.IDpCalendarService;

@Service
@PropertySource({ "classpath:application.properties" })
public class DpCalendarServiceImpl implements IDpCalendarService {

	private @Value("${dp.api.url}") String dpApiUrl;
	private @Value("${client.id}") String clientId;

	private static final Logger LOGGER = LoggerFactory.getLogger(DpCalendarServiceImpl.class);
	private static final String DEFAULT_CLIENTS = "clients";
	private static final String DEFAULT_CALENDARS = "calendars";
	private static final String DEFAULT_AUTH_BEARER = "Bearer ";
	private static final String DEFAULT_AUTH_HEADER_NAME = "Authorization";
	private static final String DEFAULT_TIMES_WITHOUT_EXCLUDED = "timeWithoutExcluded";

	private final RestTemplate restTemplate;

	@Autowired
	public DpCalendarServiceImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public int minutesBetweenDates(String token, RequestHolidayDto dto) {
		int result = 0;

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(DEFAULT_AUTH_HEADER_NAME, DEFAULT_AUTH_BEARER + token);

			UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(dpApiUrl).pathSegment(DEFAULT_CLIENTS)
					.pathSegment(clientId).pathSegment(DEFAULT_CALENDARS).pathSegment(DEFAULT_TIMES_WITHOUT_EXCLUDED)
					.queryParam("employeeId", dto.getEmployeeId())
					.queryParam("fromDate", dto.getFromDate().replace("[UTC]", ""))
					.queryParam("untilDate", dto.getUntilDate().replace("[UTC]", ""));

			LOGGER.debug(uriBuilder.toUriString());

			String prueba = uriBuilder.toUriString();

			ResponseEntity<Integer> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.POST,
					new HttpEntity<>(headers), new ParameterizedTypeReference<Integer>() {
					});

			if (response.getStatusCode().equals(HttpStatus.OK)) {
				result = response.getBody();
			}
		} catch (Exception e) {
			LOGGER.error("Failed to connect with Digital People API. DpCalendarService findOne. ");
		}

		return result;
	}

}
