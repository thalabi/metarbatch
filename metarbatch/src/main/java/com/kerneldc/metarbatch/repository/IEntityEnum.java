package com.kerneldc.metarbatch.repository;

import com.kerneldc.metarbatch.domain.AbstractEntity;

public interface IEntityEnum {

	Class<? extends AbstractEntity> getEntity();
	boolean isImmutable();
	String[] getWriteColumnOrder();
}