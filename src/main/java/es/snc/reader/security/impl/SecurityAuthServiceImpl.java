package es.snc.reader.security.impl;

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

import es.snc.reader.security.ISecurityAuthService;
import es.snc.reader.security.dto.AuthenticationRequestDto;
import es.snc.reader.security.dto.JwtTokenDto;

@Service
@PropertySource({ "classpath:application.properties" })
public class SecurityAuthServiceImpl implements ISecurityAuthService {

	private @Value("${security.api.url}") String secApiUrl;
	private @Value("${security.api.user}") String username;
	private @Value("${security.api.pass}") String password;
	private @Value("${security.api.host}") String host;

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAuthServiceImpl.class);
	private static final String DEFAULT_ENDPOINT_AUTH = "authenticate";
	private static final String DEFAULT_HEADER_HOST = "host";

	private final RestTemplate restTemplate;

	@Autowired
	public SecurityAuthServiceImpl(RestTemplate restTemplate) {

		this.restTemplate = restTemplate;
	}

	@Override
	public String authenticate() {
		String token = "";
		
		try {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(DEFAULT_HEADER_HOST, host);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(secApiUrl)
				.pathSegment(DEFAULT_ENDPOINT_AUTH);

		LOGGER.debug(uriBuilder.toUriString());

		AuthenticationRequestDto dto = new AuthenticationRequestDto(username, password);

		ResponseEntity<JwtTokenDto> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.POST,
				new HttpEntity<AuthenticationRequestDto>(dto, headers), new ParameterizedTypeReference<JwtTokenDto>() {

				});

		if (response.getStatusCode().equals(HttpStatus.OK))
			token = response.getBody().getToken();
		
		}catch (Exception e) {
			LOGGER.error("Failed to connect with Security API. SecAuthServiceImpl authenticate");
		}

		return token;
	}

}
