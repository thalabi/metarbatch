package com.kerneldc.metarbatch;


import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class FailAbortedJobs implements ApplicationRunner {

	private final MetarJobManager metarJobManager;
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		
		metarJobManager.cleanupAndRestartJobs();
	}

}
