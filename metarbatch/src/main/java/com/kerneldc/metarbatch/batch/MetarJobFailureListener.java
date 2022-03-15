package com.kerneldc.metarbatch.batch;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.core.NestedExceptionUtils;

import com.kerneldc.metarbatch.exception.ApplicationException;
import com.kerneldc.metarbatch.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class MetarJobFailureListener extends JobExecutionListenerSupport {

	private final EmailService emailService;
	
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
       	var jobParametersMap = jobExecution.getJobParameters().toProperties();
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
        
	}
}
