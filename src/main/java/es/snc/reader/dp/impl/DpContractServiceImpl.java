package es.snc.reader.dp.impl;

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

import es.snc.common.persistence.PaginatedList;
import es.snc.common.persistence.filter.FilterWithDate;
import es.snc.dp.dto.ContractDto;
import es.snc.dp.dto.EmployeeDto;
import es.snc.reader.dp.IDpContractService;
import es.snc.reader.dto.FilterEmployee;

@Service
@PropertySource({ "classpath:application.properties" })
public class DpContractServiceImpl implements IDpContractService {

	private @Value("${dp.api.url}") String dpApiUrl;
	private @Value("${client.id}") String clientId;

	private static final Logger LOGGER = LoggerFactory.getLogger(DpContractServiceImpl.class);
	private static final String DEFAULT_CLIENTS = "clients";
	private static final String DEFAULT_CONTRACTS = "contracts";
	private static final String DEFAULT_SEARCH_ALL = "searchAll";
	private static final String DEFAULT_AUTH_BEARER = "Bearer ";
	private static final String DEFAULT_AUTH_HEADER_NAME = "Authorization";

	private final RestTemplate restTemplate;

	@Autowired
	public DpContractServiceImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@Override
	public PaginatedList<ContractDto> searchContractByFilter(String token, FilterWithDate filter) {
		PaginatedList <ContractDto> result = null;
		
		try {

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(DEFAULT_AUTH_HEADER_NAME, DEFAULT_AUTH_BEARER + token);
			
			
			UriComponentsBuilder uriBuilder = UriComponentsBuilder
					.fromHttpUrl(dpApiUrl)
					.pathSegment(DEFAULT_CLIENTS)
					.pathSegment(clientId)
					.pathSegment(DEFAULT_CONTRACTS)
					.pathSegment(DEFAULT_SEARCH_ALL);
			
			LOGGER.debug(uriBuilder.toUriString());

			ResponseEntity <PaginatedList<ContractDto>> response = restTemplate.exchange(
					uriBuilder.toUriString(),
					HttpMethod.POST, 
					new HttpEntity<FilterWithDate>(filter, headers), 
					new ParameterizedTypeReference<PaginatedList<ContractDto>>() {
					});

			if (response.getStatusCode().equals(HttpStatus.OK)) {
				result = response.getBody();
			}

		} catch (Exception e) {

			LOGGER.error("Failed to connect with Digital People API. DpContractService create. ");
		}
		
		return result;
	}

}
