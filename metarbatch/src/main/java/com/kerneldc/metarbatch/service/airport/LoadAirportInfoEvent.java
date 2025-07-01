package com.kerneldc.metarbatch.service.airport;

import java.time.Clock;

import org.springframework.context.ApplicationEvent;

import com.kerneldc.metarbatch.domain.remoteapicall.RemoteApiCall;

import lombok.Getter;

public class LoadAirportInfoEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	@Getter
	private final RemoteApiCall remoteApiCall;
	
	public LoadAirportInfoEvent(Object source, RemoteApiCall remoteApiCall) {
		super(source);
		this.remoteApiCall = remoteApiCall;
	}

	public LoadAirportInfoEvent(Object source, Clock clock, RemoteApiCall remoteApiCall) {
		super(source, clock);
		this.remoteApiCall = remoteApiCall;
	}

}
