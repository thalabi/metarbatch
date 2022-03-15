package com.kerneldc.metarbatch.controller;

import javax.validation.Valid;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.metarbatch.MetarJobManager;
import com.kerneldc.metarbatch.exception.ApplicationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("metarJobLauncherController")
@RequiredArgsConstructor
@Slf4j
public class MetarJobLauncherController {

	private final MetarJobManager metarJobManager;
	
	
    @PostMapping("/launcheMetarJob")
	public ResponseEntity<Void> launcheMetarJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
    	LOGGER.info("Begin ...");
    	metarJobManager.metarJobLauncher();
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(null);
    }

    @PostMapping("/restartMetarJob")
	public ResponseEntity<Void> restartMetarJob(@Valid @RequestBody RestartMetarJobRequest restartMetarJobRequest) throws ApplicationException {
    	LOGGER.info("Begin ...");
    	metarJobManager.restartMetarJob(restartMetarJobRequest.getJobExecutionId());
    	LOGGER.info("End ...");
    	return ResponseEntity.ok(null);
    }

}
