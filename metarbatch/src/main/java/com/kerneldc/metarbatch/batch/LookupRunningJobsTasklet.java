package com.kerneldc.metarbatch.batch;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.kerneldc.metarbatch.config.BatchConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class LookupRunningJobsTasklet implements Tasklet {

	private final JobExplorer jobExplorer;
	
	public static final String METAR_JOB_PARAMETERS_SET = "metarJobParametersSet";
	public static final String IS_MULTIPLE_METAR_JOBS_RUNNING = "isMultipleMetarJobsRunning";

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		var metarJobExecutionSet = jobExplorer.findRunningJobExecutions(BatchConfig.METAR_JOB);
		LOGGER.info("metarJobExecutionSet size [{}]", metarJobExecutionSet.size());
		LOGGER.info("metarJobExecutionSet [{}]", metarJobExecutionSet);

		var jobExecutionContext = chunkContext.getStepContext()
				.getStepExecution().getJobExecution();
		
		var metarJobParametersSet = getJobParametersSetOfRunningJobs(metarJobExecutionSet, jobExecutionContext.getJobId());

		var executionContext = jobExecutionContext
				.getExecutionContext();
		executionContext.put(METAR_JOB_PARAMETERS_SET, metarJobParametersSet);
		executionContext.put(IS_MULTIPLE_METAR_JOBS_RUNNING, CollectionUtils.size(metarJobParametersSet) >= 1);
		return RepeatStatus.FINISHED;
	}

	private Set<JobParameters> getJobParametersSetOfRunningJobs(Set<JobExecution> jobExecutionSet, Long jobId) {
		var jobParametersSet = new HashSet<JobParameters>();
		for (JobExecution je: jobExecutionSet) {
			if (je.getJobId().equals(jobId)) {
				continue;
			}
			var jobParameters = je.getJobParameters();
			jobParametersSet.add(jobParameters);
		}
		return jobParametersSet;
	}
}
