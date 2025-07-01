package com.kerneldc.metarbatch.domain;

import java.util.Arrays;

import com.kerneldc.metarbatch.domain.remoteapicall.RemoteApiCall;
import com.kerneldc.metarbatch.domain.remoteapicalllog.RemoteApiCallLog;

public enum EntityEnum implements IEntityEnum {
	REMOTE_API_CALL_LOG(RemoteApiCallLog.class, false, new String[] {}),
	REMOTE_API_CALL_DETAIL(RemoteApiCall.class, false, new String[] {})
	;

	Class<? extends AbstractEntity> entity;
	boolean immutable;
	String[] writeColumnOrder;

	EntityEnum(Class<? extends AbstractEntity> entity, boolean immutable) {
		this.entity = entity;
		this.immutable = immutable;
	}
	EntityEnum(Class<? extends AbstractEntity> entity, boolean immutable, String[] writeColumnOrder) {
		this.entity = entity;
		this.immutable = immutable;
		// tag SOURCECSVLINENUMBER to the end of the writeColumnOrder
		this.writeColumnOrder = Arrays.copyOf(writeColumnOrder, writeColumnOrder.length+1);
		this.writeColumnOrder[this.writeColumnOrder.length-1] = "SOURCECSVLINENUMBER";  
	}

	@Override
	public Class<? extends AbstractEntity> getEntity() {
		return entity;
	}

	@Override
	public boolean isImmutable() {
		return immutable;
	}

	@Override
	public String[] getWriteColumnOrder() {
		return writeColumnOrder;
	}

}
