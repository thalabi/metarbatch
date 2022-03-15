package com.kerneldc.metarbatch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
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
import org.springframework.beans.factory.annotation.Value;
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
	
	private boolean metarJobExecutionEnabled;

	@PostConstruct
	public void init() {
		metarJobExecutionEnabled = false;
	}
	
	public void cleanupAbortedJobs() throws NoSuchJobException {

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
	
	public void restartFailedJobs() throws NoSuchJobException, ApplicationException {

		LOGGER.info("Restarting failed jobs");
		
		LOGGER.info("Disabling Metar job execution");
		metarJobExecutionEnabled = false;

		var metarJobExecutionList = getJobExecutions(BatchConfig.METAR_JOB);
		//LOGGER.info("metarJobExecution id list: [{}]", String.join(", ", metarJobExecutionList.stream().map(je -> je.getId().toString()).toList()));
		var completedMetarJobInstanceIdList = getCompletedJobInstanceIdList(metarJobExecutionList);
		//LOGGER.info("completedMetarJob instance id list: [{}]", String.join(", ", completedMetarJobInstanceIdList.stream().map(o -> o.toString()).toList()));
		var metarJobExecutionListToBeRestarted = removedCompletedJobExecutionList(metarJobExecutionList, completedMetarJobInstanceIdList); 
		//LOGGER.info("metarJobExecution id list to be restarted: [{}]", String.join(", ", metarJobExecutionListToBeRestarted.stream().map(je -> je.getId().toString()).toList()));
		
		for (JobExecution je : metarJobExecutionListToBeRestarted) {
			restartMetarJob(je.getId());
		}
		
		LOGGER.info("Enabling Metar job execution");
		metarJobExecutionEnabled = true;
	}

	@Scheduled(cron = "${metar.schedule.cron.expression}") // every minute
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
		var metarJobExecution = jobLauncher.run(metarJob, jpbParameters);
		LOGGER.info("MetarJob launched with job instance id: [{}]", metarJobExecution.getJobId());
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

	private List<JobExecution> getJobExecutions(String jobName) throws NoSuchJobException {
		var metarJobInstanceCount = jobExplorer.getJobInstanceCount(jobName);
		LOGGER.info("metarJobInstanceCount: [{}]", metarJobInstanceCount);
		var metarJobInstanceList = jobExplorer.findJobInstancesByJobName(jobName, 0, metarJobInstanceCount);
		var jobExecutionList = new ArrayList<JobExecution>();
		for (JobInstance ji : metarJobInstanceList) {
			var metarJobExecutionList = jobExplorer.getJobExecutions(ji);
			for (JobExecution je : metarJobExecutionList) {
				jobExecutionList.add(je);
			}
		}
		jobExecutionList.sort((a, b) -> a.getId().compareTo(b.getId()));
		return jobExecutionList;
	}

	private ArrayList<Long> getCompletedJobInstanceIdList(List<JobExecution> jobExecutionList) {
		var completedJobInstanceIdList = new ArrayList<Long>();
		for (JobExecution je : jobExecutionList) {
			if (Objects.equals(je.getStatus(), BatchStatus.COMPLETED) && Objects.equals(je.getExitStatus(), ExitStatus.COMPLETED)) {
				completedJobInstanceIdList.add(je.getJobInstance().getInstanceId());
			}
		}
		return completedJobInstanceIdList;
	}

	private List<JobExecution> removedCompletedJobExecutionList(List<JobExecution> jobExecutionList,
			ArrayList<Long> completedJobInstanceIdList) {
		var removedCompletedJobExecutionList = new ArrayList<JobExecution>();
		for (JobExecution je : jobExecutionList) {
			//LOGGER.info("completedMetarJobIdList: [{}], je.getId(): [{}], je: [{}]", completedJobInstanceIdList, je.getJobInstance().getInstanceId(), je );
			if (! /* not */ completedJobInstanceIdList.contains(je.getJobInstance().getInstanceId())) {
				removedCompletedJobExecutionList.add(je);
				//LOGGER.info("added [{}]", je);
			}
		}
		return removedCompletedJobExecutionList;
	}


}
