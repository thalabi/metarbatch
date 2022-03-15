package com.kerneldc.metarbatch.batch;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeleteFileTasklet implements Tasklet {

	private Path metarFilePath;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		getMetarFilePath(chunkContext);
		LOGGER.info("Deleting [{}]", metarFilePath);
		Files.delete(metarFilePath);
		return RepeatStatus.FINISHED;
	}

	private void getMetarFilePath(ChunkContext chunkContext) {
		ExecutionContext jobExecutionContext = chunkContext.getStepContext()
				.getStepExecution().getJobExecution()
				.getExecutionContext();
		
		metarFilePath = Path.of(jobExecutionContext.getString(DownloadTasklet.METAR_FILE_PATH_KEY));
	}

}
