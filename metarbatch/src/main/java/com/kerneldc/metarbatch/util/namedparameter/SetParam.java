package com.kerneldc.metarbatch.util.namedparameter;

import java.util.Set;

public record SetParam(String name, Set<? extends Object> value) implements NamedParameter {

	@Override
	public Class<?> getType() {
		return Set.class;
	}

}
