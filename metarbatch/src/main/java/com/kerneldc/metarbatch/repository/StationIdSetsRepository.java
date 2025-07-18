package com.kerneldc.metarbatch.repository;

import java.util.List;

import com.kerneldc.metarbatch.domain.EntityEnum;
import com.kerneldc.metarbatch.domain.IEntityEnum;
import com.kerneldc.metarbatch.domain.stationidsets.StationIdSets;

public interface StationIdSetsRepository extends BaseTableRepository<StationIdSets, Long>{

	List<StationIdSets> findByUsername(String username);
	
	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.STATION_ID_SETS;
	}

}
