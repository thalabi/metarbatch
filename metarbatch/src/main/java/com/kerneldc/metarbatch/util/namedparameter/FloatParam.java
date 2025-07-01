package com.kerneldc.metarbatch.util.namedparameter;

public record FloatParam(String name, Float value) implements NamedParameter {

	@Override
	public Class<?> getType() {
		return Float.class;
	}

}
