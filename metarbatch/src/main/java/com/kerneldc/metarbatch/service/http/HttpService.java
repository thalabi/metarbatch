package com.kerneldc.metarbatch.service.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.metarbatch.exception.ApplicationException;
import com.kerneldc.metarbatch.service.airport.AirportIdentfierName;
import com.kerneldc.metarbatch.util.namedparameter.LongParam;
import com.kerneldc.metarbatch.util.namedparameter.NamedParameter;
import com.kerneldc.metarbatch.util.namedparameter.NamedParameterSet;
import com.kerneldc.metarbatch.util.namedparameter.SetParam;
import com.kerneldc.metarbatch.util.namedparameter.StringParam;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HttpService {

	private final boolean urlLoggingEnabled;
	private final String flightLogOauth2ServerUrl;
	private final String airportInfoServiceApiUrl;

	public HttpService(@Value("${httpservice.url.logging.enabled:false}") boolean urlLoggingEnabled,
			@Value("${flightlog.oauth2.server.url}") String flightLogOauth2ServerUrl,
			@Value("${airport.info.service.url}") String airportInfoServiceApiUrl) {
		this.urlLoggingEnabled = urlLoggingEnabled;
		this.flightLogOauth2ServerUrl = flightLogOauth2ServerUrl;
		this.airportInfoServiceApiUrl = airportInfoServiceApiUrl;
	}

	public NamedParameterSet processRequest(HttpRequestTypeEnum httpRequestTypeEnum, Set<NamedParameter> data) throws ApplicationException {
		return processRequest(httpRequestTypeEnum, data, StringUtils.EMPTY);
	}
	public NamedParameterSet processRequest(HttpRequestTypeEnum httpRequestTypeEnum, String jwt) throws ApplicationException {
		return processRequest(httpRequestTypeEnum, Set.of(), jwt);
	}
	
	public NamedParameterSet processRequest(HttpRequestTypeEnum httpRequestTypeEnum, Set<NamedParameter> data, String jwt) throws ApplicationException {
		var parameterSet = new NamedParameterSet(data);
		switch (httpRequestTypeEnum) {
			case FLIGHT_LOG_JWT_TOKEN -> {
				return fetchJwtAccessToken(parameterSet);
			}
			case AIRPORT_INFO -> {
				return loadAirportInfoFromExternalApi(jwt);
			}
		}
		return null;
	}
	
	private NamedParameterSet loadAirportInfoFromExternalApi(String jwt) throws ApplicationException  {
		
		if (urlLoggingEnabled) LOGGER.info("Hitting url: [{}]", airportInfoServiceApiUrl);

		var client = HttpClient.newHttpClient();
		var httpRequest = HttpRequest.newBuilder()
		    .uri(URI.create(airportInfoServiceApiUrl))
		    .GET()
			.header("Authorization", "Bearer " + jwt)
			.build();

		HttpResponse<String> response = null;
		try {
			response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			var message = String.format("Fetching contents of URL [%s] failed. ", airportInfoServiceApiUrl);
			throw new ApplicationException(message, e);
		}
		var httpStatusCode = response.statusCode();
		if (httpStatusCode != HttpURLConnection.HTTP_OK) {
			var message = String.format("Fetching contents of URL [%s], returned Http status code [%d]. ",
					airportInfoServiceApiUrl, httpStatusCode);
			if (response != null && StringUtils.isNotEmpty(response.body())) {
				message += String.format("Site message: [%s]", response.body());
			}
			throw new ApplicationException(message);
		}

		var jsonResponse = response.body();
		var mapper = new ObjectMapper();
		JsonNode root;
		try {
			root = mapper.readTree(jsonResponse);
		} catch (JsonProcessingException e) {
			var message = String.format("Exception when parsing json response: %s", jsonResponse);
			LOGGER.error(message, e);
			throw new ApplicationException(message);
		}
		LOGGER.debug(root.toPrettyString());
		var identifiersAndNames = root.path("identifiersAndNames");
		Set<AirportIdentfierName> airportInfoSet = new HashSet<>();
		if (identifiersAndNames.isArray()) {
			for (JsonNode identifiersAndName : identifiersAndNames) {
				
				airportInfoSet.add(new AirportIdentfierName(
						identifiersAndName.path("identifier").asText(),
						identifiersAndName.path("name").asText()));
			}
		}
		
		new SetParam("airportInfoSet", airportInfoSet);
		return new NamedParameterSet(Set.of(new SetParam("airportInfoSet", airportInfoSet)));
	}

	private NamedParameterSet fetchJwtAccessToken(NamedParameterSet parameters) throws ApplicationException {
		
		if (urlLoggingEnabled) LOGGER.info("Hitting url: [{}]", flightLogOauth2ServerUrl);
		
		var oauth2ClientId = parameters.get("oauth2clientId", String.class);
		var oauth2ClientSecret = parameters.get("oauth2ClientSecret", String.class);
		
		var client = HttpClient.newHttpClient();
		var request = HttpRequest.newBuilder()
		    .uri(URI.create(flightLogOauth2ServerUrl))
		    .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED.toString())
			.POST(BodyPublishers.ofString("client_id=" + oauth2ClientId + "&client_secret=" + oauth2ClientSecret
					+ "&grant_type=client_credentials"))
		    .build();

		HttpResponse<String> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			var message = String.format("Fetching contents of URL [%s] failed. ", flightLogOauth2ServerUrl);
			throw new ApplicationException(message, e);
		}
		var httpStatusCode = response.statusCode();
		if (httpStatusCode != HttpURLConnection.HTTP_OK) {
			var message = String.format("Fetching contents of URL [%s], returned Http status code [%d]. ",
					flightLogOauth2ServerUrl, httpStatusCode);
			if (response != null && StringUtils.isNotEmpty(response.body())) {
				message += String.format("Site message: [%s]", response.body());
			}
			throw new ApplicationException(message);
		}
		var jsonResponse = response.body();
		var mapper = new ObjectMapper();
		JsonNode root;
		try {
			root = mapper.readTree(jsonResponse);
		} catch (JsonProcessingException e) {
			var message = String.format("Exception when parsing json response: %s", jsonResponse);
			LOGGER.error(message, e);
			throw new ApplicationException(message);
		}
		LOGGER.debug(root.toPrettyString());
		
		var jwt = root.path("access_token").asText();
		var expiresIn = root.path("expires_in").asLong();
		return new NamedParameterSet(Set.of(new StringParam("jwt", jwt), new LongParam("expiresIn", expiresIn)));
	}

}
