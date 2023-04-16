package com.kerneldc.metarbatch;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

import javax.annotation.PostConstruct;

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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kerneldc.metarbatch.config.BatchConfig;
import com.kerneldc.metarbatch.exception.ApplicationException;
import com.kerneldc.metarbatch.service.EmailService;
import com.kerneldc.metarbatch.util.TimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class MetarJobManager {

	private static final Date JOB_END_TIME = TimeUtils.toDate(LocalDateTime.of(9999, 12, 31, 23, 59, 59));

//	private static final String RESTART_JOBS_SQL = """
//		with
//		job_instance as (
//			select bji.job_instance_id id from batch_job_instance bji where bji.job_name = ?),
//		failed_execution as (
//			select job_execution_id, job_instance_id from batch_job_execution bje join job_instance on bje.job_instance_id = job_instance.id where bje.status != 'COMPLETED' and bje.status != 'ABANDONED'),
//		completed_execution as (
//			select job_execution_id, job_instance_id from batch_job_execution bje join job_instance on bje.job_instance_id = job_instance.id where bje.status = 'COMPLETED'),
//		execution_to_restart as (
//			select max(job_execution_id) last_job_execution_id, job_instance_id from failed_execution fe where fe.job_instance_id not in (select ce.job_instance_id from completed_execution ce)
//				group by job_instance_id
//				order by last_job_execution_id)
//		select last_job_execution_id from execution_to_restart
//	""";
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
	@Value("${noaa.server.resource:url:https://aviationweather.gov/adds/dataserver_current/current/metars.cache.xml.gz}")
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
	public void cleanupAndRestartJobs() throws NoSuchJobException, ApplicationException {
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
		} catch (JobInstanceAlreadyCompleteException | NoSuchJobExecutionException | NoSuchJobException
				| JobRestartException | JobParametersInvalidException e) {
			var stacktrace = ExceptionUtils.getStackTrace(e);
			LOGGER.error(stacktrace);
			emailService.sendMetarJobRestartFailure(jobExecutionId, stacktrace);
		}
	}

	private void cleanupAbortedJobs() throws NoSuchJobException {

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
		
		LOGGER.info("Enabling Metar job execution");
		metarJobExecutionEnabled = true;
	}
	
	private void restartFailedJobs() throws NoSuchJobException, ApplicationException {

		LOGGER.info("Restarting failed jobs");
		
		LOGGER.info("Disabling Metar job execution");
		metarJobExecutionEnabled = false;

		var jobExecutionIdListToBeRestarted = batchJdbcTemplate.queryForList(RESTART_JOBS_SQL, Long.class, BatchConfig.METAR_JOB);
		for (Long jobExecutionId : jobExecutionIdListToBeRestarted) {
			restartMetarJob(jobExecutionId);
		}
		
		LOGGER.info("Enabling Metar job execution");
		metarJobExecutionEnabled = true;
	}

}
