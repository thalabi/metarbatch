package com.kerneldc.metarbatch;


import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.kerneldc.metarbatch.exception.ApplicationException;
import com.kerneldc.metarbatch.service.MetarPartitionService;
import com.kerneldc.metarbatch.service.airport.AirportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class StartupTasks {

	private final MetarPartitionService metarPartitionService;
	private final MetarJobManager metarJobManager;
	private final AirportService airportService;
	
	@EventListener(ApplicationReadyEvent.class)
	public void createPartitionsAndCleanJobs() throws ApplicationException {
		
		LOGGER.info("Running startup tasks ...");
		
		metarPartitionService.createThisMonthPartition();
		metarPartitionService.createNextMonthPartition();
		
		metarJobManager.cleanupAndRestartJobs();
		
		airportService.loadAirportInfoFromExternalApi();

		LOGGER.info("Startup tasks completed");
	}

}
