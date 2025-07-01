package com.kerneldc.metarbatch.util.namedparameter;

public record StringParam(String name, String value) implements NamedParameter {

	@Override
	public String getName() {
		return name();
	}

	@Override
	public Object getValue() {
		return value();
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

}
