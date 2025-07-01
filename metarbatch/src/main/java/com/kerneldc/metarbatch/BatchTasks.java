package com.kerneldc.metarbatch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kerneldc.metarbatch.service.airport.AirportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchTasks {
	
	private final AirportService airportService;
	
	@Scheduled(cron = "0 40 7 * * SUN") // Every Sun at 7:40 AM (after Jenkins job that enriches airport table)
	public void refreshAirportInfo() {
		airportService.refreshAirportInfoFromExternalApi();
	}
	
}
