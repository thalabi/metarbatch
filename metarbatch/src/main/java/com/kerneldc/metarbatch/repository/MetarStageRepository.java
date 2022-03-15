package com.kerneldc.metarbatch.repository;

import com.kerneldc.metarbatch.domain.MetarStage;

public interface MetarStageRepository extends BaseTableRepository<MetarStage, Long> {
	
	@Override
	default IEntityEnum canHandle() {
		return MetarTableEnum.METAR_STAGE;
	}
	
}
