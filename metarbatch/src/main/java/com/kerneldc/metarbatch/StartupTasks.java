package com.kerneldc.metarbatch;


import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.kerneldc.metarbatch.service.MetarPartitionService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class StartupTasks implements ApplicationRunner {

	private final MetarJobManager metarJobManager;
	private final MetarPartitionService metarPartitionService;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		
		metarPartitionService.createThisMonthPartition();
		metarPartitionService.createNextMonthPartition();
		
		metarJobManager.cleanupAndRestartJobs();
	}

}
