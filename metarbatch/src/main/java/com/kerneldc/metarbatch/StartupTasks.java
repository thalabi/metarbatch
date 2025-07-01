package com.kerneldc.metarbatch;


import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.kerneldc.metarbatch.exception.ApplicationException;
import com.kerneldc.metarbatch.service.MetarPartitionService;
import com.kerneldc.metarbatch.service.airport.AirportService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class StartupTasks {

	private final MetarPartitionService metarPartitionService;
	private final MetarJobManager metarJobManager;
	private final AirportService airportService;
	
	@EventListener(ApplicationReadyEvent.class)
	public void createPartitionsAndCleanJobs() throws ApplicationException {
		
		metarPartitionService.createThisMonthPartition();
		metarPartitionService.createNextMonthPartition();
		
		metarJobManager.cleanupAndRestartJobs();
		
		airportService.loadAirportInfoFromExternalApi();
	}

}
