package es.snc.reader.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import es.snc.reader.excel.IExcelReader;
import es.snc.reader.security.ISecurityAuthService;
import es.snc.reader.security.dto.JwtTokenDto;
import es.snc.reader.service.IService;

@Service
@PropertySource({ "classpath:application.properties" })
public class ServiceImpl implements IService {

	private @Value("${client.id}") String clientId;
	private @Value("${excelAbsolutePath}") String fileAbsolutePath;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceImpl.class);

	@Autowired
	private IExcelReader excelReaderService;

	@Autowired
	private ISecurityAuthService securityAuthService;

	JwtTokenDto token;

	@Autowired
	public ServiceImpl() {
	}

	@Override
	public void startService() {

		token = new JwtTokenDto();
		token.setToken(securityAuthService.authenticate());

		if (token.getToken() != null) {

			try {

				excelReaderService.readExcelSheets(token);
			} catch (FileNotFoundException e) {

				LOGGER.error("Error. File not found exception. ", e);
			} catch (IOException e) {

				LOGGER.error("Error. Input/Output exception.", e);
			}
		}

	}

}
