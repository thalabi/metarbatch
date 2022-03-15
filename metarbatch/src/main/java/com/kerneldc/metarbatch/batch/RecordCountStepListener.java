
package com.kerneldc.metarbatch.batch;

import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecordCountStepListener extends StepExecutionListenerSupport {

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
        var exitCode = stepExecution.getExitStatus().getExitCode();
        if (StringUtils.equals(exitCode, ExitStatus.FAILED.getExitCode()) && stepExecution.getSkipCount() > 0) {
        	LOGGER.warn("Step completed with [{}] skiped items", stepExecution.getSkipCount());
            return new ExitStatus("Completed with skips");
        } else {
        	LOGGER.info("Step [{}] read count: [{}], write count: [{}], commit count: [{}]", stepExecution.getStepName(), stepExecution.getReadCount(), stepExecution.getWriteCount(), stepExecution.getCommitCount());
            return null;
        }
    }

}
