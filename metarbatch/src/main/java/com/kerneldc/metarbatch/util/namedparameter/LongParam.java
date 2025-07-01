package com.kerneldc.metarbatch.util.namedparameter;

public record LongParam(String name, Long value) implements NamedParameter {

	@Override
	public Class<?> getType() {
		return Long.class;
	}

}
