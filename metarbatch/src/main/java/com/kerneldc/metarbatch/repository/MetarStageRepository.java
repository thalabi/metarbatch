package com.kerneldc.metarbatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kerneldc.metarbatch.domain.MetarStage;

public interface MetarStageRepository extends /*BaseTableRepository*/ JpaRepository<MetarStage, Long> {
	
//	@Override
//	default IEntityEnum canHandle() {
//		return MetarTableEnum.METAR_STAGE;
//	}
	
}
