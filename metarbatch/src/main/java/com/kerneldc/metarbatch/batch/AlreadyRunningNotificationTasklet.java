package com.kerneldc.metarbatch.batch;

import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.kerneldc.metarbatch.exception.ApplicationException;
import com.kerneldc.metarbatch.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class AlreadyRunningNotificationTasklet implements Tasklet {

	private final EmailService emailService;
	private final DefaultJobParametersConverter defaultJobParametersConverter;
	
	@SuppressWarnings("unchecked")
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		LOGGER.info("execute()");
	
		var currentJobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution(); 
		var currentJobParameters = currentJobExecution.getJobParameters();

		var executionContext = currentJobExecution.getExecutionContext();
		var metarJobParametersSet = (Set<JobParameters>)executionContext.get(LookupRunningJobsTasklet.METAR_JOB_PARAMETERS_SET);
		Validate.isTrue(CollectionUtils.size(metarJobParametersSet) == 1, "metarJobParametersSet size is %d, expected 1", CollectionUtils.size(metarJobParametersSet));
		var runningJobParameters = metarJobParametersSet.iterator().next();
				
//		var currentJobParametersMap = currentJobParameters.toProperties();
		var currentJobParametersMap = defaultJobParametersConverter.getProperties(currentJobParameters);
//		var runningJobParametersMap = runningJobParameters.toProperties();
		var runningJobParametersMap = defaultJobParametersConverter.getProperties(runningJobParameters);
		
		LOGGER.info("currentJobParametersMap [{}]", currentJobParametersMap);
		LOGGER.info("runningJobParametersMap [{}]", runningJobParametersMap);

		emailService.sendMetarJobAlreadyRunning(currentJobParametersMap, runningJobParametersMap);
		
		throw new ApplicationException("Metar job already running");
	}
}
