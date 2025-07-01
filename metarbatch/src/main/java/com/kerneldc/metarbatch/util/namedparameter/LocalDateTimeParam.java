package com.kerneldc.metarbatch.util.namedparameter;

import java.time.LocalDateTime;

public record LocalDateTimeParam(String name, LocalDateTime value) implements NamedParameter {

	@Override
	public Class<?> getType() {
		return LocalDateTime.class;
	}

}
