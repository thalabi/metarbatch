package com.kerneldc.metarbatch.batch;



import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.kerneldc.metarbatch.repository.MetarStageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DeleteMetarStageTasklet implements Tasklet {

	private final MetarStageRepository metarStageRepository;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		LOGGER.info("Deleting metar_stage table");
		metarStageRepository.deleteAllInBatch();
		return RepeatStatus.FINISHED;
	}

}
