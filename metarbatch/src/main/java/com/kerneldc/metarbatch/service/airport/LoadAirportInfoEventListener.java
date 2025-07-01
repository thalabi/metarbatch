package com.kerneldc.metarbatch.service.airport;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.kerneldc.metarbatch.exception.ApplicationException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoadAirportInfoEventListener {

	private final AirportInfoLoader airportInfoLoader;

	@Async
	@EventListener
	public void loadEventListener(LoadAirportInfoEvent loadAirportInfoEvent) throws ApplicationException {
		airportInfoLoader.loadRemotely(loadAirportInfoEvent);
	}
}
