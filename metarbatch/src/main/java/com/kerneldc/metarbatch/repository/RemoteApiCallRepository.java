package com.kerneldc.metarbatch.repository;

import com.kerneldc.metarbatch.domain.EntityEnum;
import com.kerneldc.metarbatch.domain.IEntityEnum;
import com.kerneldc.metarbatch.domain.remoteapicall.RemoteApiCall;

public interface RemoteApiCallRepository extends BaseTableRepository<RemoteApiCall, Long>{

	@Override
	default IEntityEnum canHandle() {
		return EntityEnum.REMOTE_API_CALL_DETAIL;
	}

}
