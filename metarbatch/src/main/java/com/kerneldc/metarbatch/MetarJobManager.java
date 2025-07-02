package com.kerneldc.metarbatch;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kerneldc.metarbatch.config.BatchConfig;
import com.kerneldc.metarbatch.exception.ApplicationException;
import com.kerneldc.metarbatch.service.EmailService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetarJobManager {

	private static final LocalDateTime JOB_END_TIME = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

	private static final String RESTART_JOBS_SQL = """
		with
		failed_job_instances as (
			select bji.job_instance_id
			  from batch_job_instance bji
			 where bji.job_name = ? and not exists (select * from batch_job_execution bje where bje.job_instance_id = bji.job_instance_id and (status = 'COMPLETED' or status = 'ABANDONED'))),
		latest_job_execution as (
			select max(bje2.job_execution_id) max_job_execution_id, bje2.job_instance_id
			  from batch_job_execution bje2
			  join failed_job_instances fji on bje2.job_instance_id = fji.job_instance_id
			 group by bje2.job_instance_id)
		select max_job_execution_id from latest_job_execution
		""";
	public static final String JOB_TIMESTAMP ="jobTimestamp";
	@Value("${noaa.server.resource}")
	private String noaaServerResouce;
	@Value("${work.directory}")
	private String workDirectory;
	@Value("${metar.file}")
	private String metarFile;
	private final JobRepository jobRepository;
	private final JobLauncher jobLauncher;
	private final JobExplorer jobExplorer;
	private final JobOperator jobOperator;
	private final Job metarJob;
	private final EmailService emailService;
	
	@Qualifier("batchJdbcTemplate")
	private final JdbcTemplate batchJdbcTemplate;
	
	private boolean metarJobExecutionEnabled;

	@PostConstruct
	public void init() {
		metarJobExecutionEnabled = false;
	}
	
	@Scheduled(cron = "${cleanup.schedule.cron.expression}")
	public void cleanupAndRestartJobs() throws ApplicationException {
		cleanupAbortedJobs();
		restartFailedJobs();
	}
	
	@Scheduled(cron = "${metar.schedule.cron.expression}")
	public void metarJobLauncher() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		
		if (! /* not */ metarJobExecutionEnabled) {
			LOGGER.info("MetarJobScheduling is disabled");
			return;
		}
		
		var jpbParameters = new JobParametersBuilder(jobExplorer)
				.addDate(JOB_TIMESTAMP, new Date())
				.addString("noaaServerResouce", noaaServerResouce)
				.addString("workDirectory", workDirectory)
				.addString("metarFile", metarFile)
				.getNextJobParameters(metarJob)
				.toJobParameters();
		LOGGER.info("Launching MetarJob ...");
		var metarJobExecution = jobLauncher.run(metarJob, jpbParameters);
		LOGGER.info("MetarJob was launched. Instance id [{}] and status [{}]", metarJobExecution.getJobId(), metarJobExecution.getStatus());
	}
	
	public void restartMetarJob(Long jobExecutionId) throws ApplicationException {
		try {
			LOGGER.info("Restarting failed Metar job (execution id [{}])", jobExecutionId);
			var metarJobExecutionId = jobOperator.restart(jobExecutionId);
			LOGGER.info("Metar job completed (new execution id [{}])", metarJobExecutionId);
		} catch (JobInstanceAlreadyCompleteException e) {
			LOGGER.warn("Unable to restart job execution id: {} due to JobInstanceAlreadyCompleteException exception. Marking it as ABANDONED.", jobExecutionId);
			markJobAsAbandoned(jobExecutionId);
			emailService.sendMetarJobSetToAbandoned(jobExecutionId);
		} catch (/*JobInstanceAlreadyCompleteException | */ NoSuchJobExecutionException | NoSuchJobException
				| JobRestartException | JobParametersInvalidException e) {
			var stacktrace = ExceptionUtils.getStackTrace(e);
			LOGGER.error(stacktrace);
			emailService.sendMetarJobRestartFailure(jobExecutionId, stacktrace);
		}
	}

	private void markJobAsAbandoned(Long jobExecutionId) {
		var jobExecution = jobExplorer.getJobExecution(jobExecutionId);
		jobExecution.setStatus(BatchStatus.ABANDONED);
		jobRepository.update(jobExecution);
	}

	private void cleanupAbortedJobs() {

		LOGGER.info("Cleanup aborted jobs");
		
		LOGGER.info("Disabling Metar job execution");
		metarJobExecutionEnabled = false;
		

		var metarJobExecutionSet = jobExplorer.findRunningJobExecutions(BatchConfig.METAR_JOB);
		
		for (JobExecution je: metarJobExecutionSet) {
			LOGGER.info("Marking job execution id: [{}], instance id: [{}] as FAILED", je.getId(), je.getJobInstance().getId(), je.getStatus());
			
			je.setEndTime(JOB_END_TIME);
			je.setExitStatus(ExitStatus.FAILED);
			je.setStatus(BatchStatus.FAILED);
			
			for (StepExecution se : je.getStepExecutions()) {
				if (Objects.equals(se.getStatus(), BatchStatus.STARTED)) {
					se.setStatus(BatchStatus.FAILED);
					se.setExitStatus(ExitStatus.FAILED);
					jobRepository.update(se);
				}
			}
			jobRepository.update(je);
		}
		
		enableMetarJobExecution();
	}
	
	private void restartFailedJobs() throws ApplicationException {

		LOGGER.info("Restarting failed jobs");
		
		LOGGER.info("Disabling Metar job execution");
		metarJobExecutionEnabled = false;

		var jobExecutionIdListToBeRestarted = batchJdbcTemplate.queryForList(RESTART_JOBS_SQL, Long.class, BatchConfig.METAR_JOB);
		for (Long jobExecutionId : jobExecutionIdListToBeRestarted) {
			try {
				restartMetarJob(jobExecutionId);
			} catch (ApplicationException e) {
				enableMetarJobExecution();
				throw e;
			}
		}
		
		enableMetarJobExecution();
	}

	private void enableMetarJobExecution() {
		LOGGER.info("Enabling Metar job execution");
		metarJobExecutionEnabled = true;
	}

}
