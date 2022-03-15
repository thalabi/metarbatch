package com.kerneldc.metarbatch.repository;

import com.kerneldc.metarbatch.domain.AbstractEntity;
import com.kerneldc.metarbatch.domain.MetarStage;

public enum MetarTableEnum implements IEntityEnum {
	METAR_STAGE(MetarStage.class, false, new String[] {})
	;

	Class<? extends AbstractEntity> entity;
	boolean immutable;
	String[] writeColumnOrder;
	
	MetarTableEnum(Class<? extends AbstractEntity> entity, boolean immutable) {
		this.entity = entity;
		this.immutable = immutable;
	}
	MetarTableEnum(Class<? extends AbstractEntity> entity, boolean immutable, String[] writeColumnOrder) {
		this.entity = entity;
		this.immutable = immutable;
		this.writeColumnOrder = writeColumnOrder;
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
	
//	public static IEntityEnum valueIfPresent(String name) {
//	    return Enums.getIfPresent(MetarTableEnum.class, name).orNull();
//	}

}
