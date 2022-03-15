package com.kerneldc.metarbatch.batch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.item.ExecutionContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultipleRunningJobsDecider implements JobExecutionDecider {

	public static final String RUNNING = "RUNNING";
	public static final String NOT_RUNNING = "NOT_RUNNING";
	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
		var isMetarJobRunning = (Boolean)executionContext.get(LookupRunningJobsTasklet.IS_MULTIPLE_METAR_JOBS_RUNNING);
		if (isMetarJobRunning == null) {
			return new FlowExecutionStatus(NOT_RUNNING);
		}
		
		executionContext.remove(LookupRunningJobsTasklet.IS_MULTIPLE_METAR_JOBS_RUNNING);
		
		LOGGER.info("isMetarJobRunning: [{}]", isMetarJobRunning);
		
		if (isMetarJobRunning) {
			return new FlowExecutionStatus(RUNNING);
		} else {
			return new FlowExecutionStatus(NOT_RUNNING);
		}
	}


}
