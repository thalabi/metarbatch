package com.kerneldc.metarbatch.service;

import java.time.LocalDateTime;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kerneldc.metarbatch.exception.ApplicationException;
import com.kerneldc.metarbatch.service.http.HttpRequestTypeEnum;
import com.kerneldc.metarbatch.service.http.HttpService;
import com.kerneldc.metarbatch.util.namedparameter.StringParam;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtTokenService {

	private final String flightLogOauth2ClientId;
	private final String flightLogOauth2ClientSecret;
	
	private final HttpService httpService;
	
	private String jwt;
	private LocalDateTime jwtExpirationTime;

	public JwtTokenService(
			@Value("${flightlog.oauth2.client.id}") String flightLogOauth2ClientId,
			@Value("${flightlog.oauth2.client.secret}") String flightLogOauth2ClientSecret,
			HttpService httpService) {
		this.flightLogOauth2ClientId = flightLogOauth2ClientId;
		this.flightLogOauth2ClientSecret = flightLogOauth2ClientSecret;
		this.httpService = httpService;
	}

	public String getJwtToken() throws ApplicationException {
		if (StringUtils.isEmpty(jwt) || LocalDateTime.now().isAfter(jwtExpirationTime)) {
			LOGGER.info("No JWT token or it is expired, requesting one");
			fetchJwtAccessToken();
		} else {
			LOGGER.info("JWT token is still valid");
		}
		return jwt;
	}

	private void fetchJwtAccessToken() throws ApplicationException {
		var jwtTokenRequestTime = LocalDateTime.now();
		var returnParams = httpService.processRequest(HttpRequestTypeEnum.FLIGHT_LOG_JWT_TOKEN,
				Set.of(new StringParam("oauth2clientId", flightLogOauth2ClientId),
						new StringParam("oauth2ClientSecret", flightLogOauth2ClientSecret)));
		jwt = returnParams.get("jwt", String.class);
		var expiresIn = returnParams.get("expiresIn", Long.class);
		LOGGER.info("JWT token [{}]", jwt);
		jwtExpirationTime = jwtTokenRequestTime.plusSeconds(expiresIn);
		LOGGER.info("JWT token expires in [{}] seconds, at [{}]", expiresIn, jwtExpirationTime);
	}

}
