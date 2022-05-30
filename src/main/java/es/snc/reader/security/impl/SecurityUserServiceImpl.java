package es.snc.reader.security.impl;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import es.snc.dp.dto.EmployeeDto;
import es.snc.reader.dto.FilterUserDto;
import es.snc.reader.security.ISecurityUserService;
import es.snc.security.dto.UserDto;
import es.snc.security.filter.UserFilter;

@Service
@PropertySource({ "classpath:application.properties" })
public class SecurityUserServiceImpl implements ISecurityUserService{

	private @Value("${security.api.url}") String secApiUrl;

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityUserServiceImpl.class);
	private static final String DEFAULT_USERS = "users";
	private static final String DEFAULT_SEARCH_ONE = "search-one";
	private static final String DEFAULT_AUTH_BEARER = "Bearer ";
	private static final String DEFAULT_AUTH_HEADER_NAME = "Authorization";

	
	private final RestTemplate restTemplate;

	@Autowired
	public SecurityUserServiceImpl(RestTemplate restTemplate) {

		this.restTemplate = restTemplate;
	}

	@Override
	public UserDto findOneByFilter(FilterUserDto filter, String token) {
		UserDto user=null;
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(DEFAULT_AUTH_HEADER_NAME, DEFAULT_AUTH_BEARER + token);

		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromHttpUrl(secApiUrl)
				.pathSegment(DEFAULT_USERS)
				.pathSegment(DEFAULT_SEARCH_ONE);
		
		
		LOGGER.debug(uriBuilder.toUriString());

		ResponseEntity <UserDto> response = restTemplate.exchange(
				uriBuilder.toUriString(),
				HttpMethod.POST, 
				new HttpEntity<FilterUserDto>(filter, headers), 
				new ParameterizedTypeReference<UserDto>() {
				});
		
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			user = response.getBody();
		}

		return user;
	}

}
