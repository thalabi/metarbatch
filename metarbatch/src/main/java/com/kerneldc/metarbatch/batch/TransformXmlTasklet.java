package com.kerneldc.metarbatch.batch;

import java.nio.file.Path;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import com.kerneldc.metarbatch.xml.schema.metar.METAR;
import com.kerneldc.metarbatch.xml.schema.metar.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransformXmlTasklet implements Tasklet {

	private Path metarFilePath;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		getMetarFilePath(chunkContext);
		LOGGER.info("Retrieved metarFilePath [{}] from execution context", metarFilePath);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(Response.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

		Response response = (Response) jaxbUnmarshaller.unmarshal(metarFilePath.toFile());
		List<METAR> metarList = response.getData().getMETAR();
		LOGGER.info("Transformed xml file to [{}] metar objects", metarList.size());
		
		saveMetarList(chunkContext, metarList);

		return RepeatStatus.FINISHED;
	}

	private void saveMetarList(ChunkContext chunkContext, List<METAR> metarList) {
		ExecutionContext jobExecutionContext = chunkContext.getStepContext()
				.getStepExecution().getJobExecution()
				.getExecutionContext();
		jobExecutionContext.put("metarList", metarList);		
	}

	private void getMetarFilePath(ChunkContext chunkContext) {
		ExecutionContext jobExecutionContext = chunkContext.getStepContext()
				.getStepExecution().getJobExecution()
				.getExecutionContext();
		
		metarFilePath = Path.of(jobExecutionContext.getString(DownloadTasklet.METAR_FILE_PATH_KEY));
	}
}
