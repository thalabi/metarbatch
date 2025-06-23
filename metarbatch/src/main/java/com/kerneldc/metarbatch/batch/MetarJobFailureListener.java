package com.kerneldc.metarbatch.batch;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.core.NestedExceptionUtils;

import com.kerneldc.metarbatch.exception.ApplicationException;
import com.kerneldc.metarbatch.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class MetarJobFailureListener implements JobExecutionListener {

	private final EmailService emailService;
	private final JobExplorer jobExplorer;
	private final JobRepository jobRepository;
	private final int maxAttemptsToAbandon;
	private final DefaultJobParametersConverter defaultJobParametersConverter;

	
	@Override
	public void afterJob(JobExecution jobExecution) {
		
		LOGGER.info("jobExecution.getStatus(): [{}]", jobExecution.getStatus());

		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            LOGGER.info("Metar job executed successfully");
            return;
        }
		
		var stacktraceList = new ArrayList<String>();
       	LOGGER.error("Metar job failed");
       	
       	LOGGER.error("Job parameters:");
//       	var jobParametersMap = jobExecution.getJobParameters().toProperties();
		var jobParametersMap = defaultJobParametersConverter.getProperties(jobExecution.getJobParameters());

       	for (Map.Entry<Object, Object> entry : jobParametersMap.entrySet()) {
       		LOGGER.error("Parameter name: [{}], value: [{}]", entry.getKey(), entry.getValue());
       	}
       	
       	LOGGER.error("Exception(s):");
		for (Throwable throwable : jobExecution.getAllFailureExceptions()) {
			var mostSpecificThrowable = NestedExceptionUtils.getMostSpecificCause(throwable);
			var stacktrace = ExceptionUtils.getStackTrace(mostSpecificThrowable);
			LOGGER.error(stacktrace);
			stacktraceList.add(stacktrace);
		}
		
		try {
			emailService.sendMetarJobFailure(jobParametersMap, stacktraceList);
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		markJobAsAbandoned(jobExecution);
	}


	private void markJobAsAbandoned(JobExecution jobExecution) {
		
		var thisJobExecutions = jobExplorer.getJobExecutions(jobExecution.getJobInstance());
		if (thisJobExecutions.size() >= maxAttemptsToAbandon) {
			LOGGER.info("{} {}", thisJobExecutions.size(), maxAttemptsToAbandon);
			LOGGER.info("Maximum attempts to run metar job, instance id: [{}], has been reached", jobExecution.getJobInstance());
			for (JobExecution je : thisJobExecutions) {
				LOGGER.info("Setting job execution id: [{}] to ABANDONED", je.getId());
				je.setStatus(BatchStatus.ABANDONED);
				jobRepository.update(je);
			}
			// TODO send email notification
		}
		
	}
}
