package com.kerneldc.metarbatch.repository;

import com.kerneldc.metarbatch.domain.EntityEnum;
import com.kerneldc.metarbatch.domain.IEntityEnum;
import com.kerneldc.metarbatch.domain.remoteapicalllog.RemoteApiCallLog;

public interface RemoteApiCallLogRepository extends BaseTableRepository<RemoteApiCallLog, Long>{

	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.REMOTE_API_CALL_LOG;
	}

}
