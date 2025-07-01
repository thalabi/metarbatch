package com.kerneldc.metarbatch.util.namedparameter;

public record BooleanParam(String name, Boolean value) implements NamedParameter {

	@Override
	public Class<?> getType() {
		return Boolean.class;
	}

}
