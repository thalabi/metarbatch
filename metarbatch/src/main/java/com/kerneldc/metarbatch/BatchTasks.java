package com.kerneldc.metarbatch;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kerneldc.metarbatch.exception.ApplicationException;
import com.kerneldc.metarbatch.service.MetarPartitionService;
import com.kerneldc.metarbatch.service.airport.AirportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchTasks {
	
	private final AirportService airportService;
	private final MetarPartitionService metarPartitionService;
	
	@Scheduled(cron = "${refresh.airport.info.schedule.cron.expression}")
	public void refreshAirportInfo() {
		airportService.refreshAirportInfoFromExternalApi();
	}
	
	@Scheduled(cron = "${create.partition.schedule.cron.expression}")
	public void createNextMonthPartition() throws ApplicationException {
		metarPartitionService.createNextMonthPartition();
	}

}
