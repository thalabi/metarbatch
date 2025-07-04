package com.kerneldc.metarbatch.service.http;

import lombok.Getter;

public enum HttpRequestTypeEnum {
	FLIGHT_LOG_JWT_TOKEN("Flight Log OAuth2 JWT Access Token Service"),
	AIRPORT_INFO("Airport Information Service");
	
	@Getter
	String serviceDescription;
	
	HttpRequestTypeEnum(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}
}