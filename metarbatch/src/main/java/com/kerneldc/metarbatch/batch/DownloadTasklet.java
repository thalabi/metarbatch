package com.kerneldc.metarbatch.batch;

import java.time.LocalDateTime;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import com.kerneldc.metarbatch.service.MetarService;
import com.kerneldc.metarbatch.util.TimeUtils;

import liquibase.util.Validate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DownloadTasklet implements Tasklet {

	public static final String METAR_FILE_PATH_KEY = "metarFilePath";
	
	private final MetarService metarService;
	private LocalDateTime jobTimestamp;
	private String noaaServerResouce;
	private String workDirectory;
	private String metarFile;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		getJobParameters(chunkContext);

		var metarZipInputStream = metarService.downloadMetarZipFile(noaaServerResouce);
		var metarFilePath = metarService.unzipMetarFile(metarZipInputStream, workDirectory, metarFile, jobTimestamp);
		LOGGER.info("Size of downloaded unzipped file [{}] is [{}]", metarFilePath, metarFilePath.toFile().length());
		
		ExecutionContext executionContext = chunkContext.getStepContext()
				.getStepExecution().getJobExecution()
				.getExecutionContext();
		executionContext.putString(METAR_FILE_PATH_KEY, metarFilePath.toString());

		return RepeatStatus.FINISHED;
	}

	private void getJobParameters(ChunkContext chunkContext) {
		var jobTimestampDate = chunkContext.getStepContext()
				.getStepExecution()
				.getJobParameters()
				.getDate("jobTimestamp");
		Validate.notNull(jobTimestampDate, "jobTimestamp job parameter not set");
		jobTimestamp = TimeUtils.toLocalDateTime(jobTimestampDate);

		noaaServerResouce = chunkContext.getStepContext()
				.getStepExecution()
				.getJobParameters()
				.getString("noaaServerResouce");
		workDirectory = chunkContext.getStepContext()
				.getStepExecution()
				.getJobParameters().getString("workDirectory");
		metarFile = chunkContext.getStepContext()
				.getStepExecution()
				.getJobParameters().getString("metarFile");
	}
}
