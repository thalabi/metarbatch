package com.kerneldc.metarbatch.util.namedparameter;

public sealed interface NamedParameter permits StringParam, BooleanParam, IntegerParam, FloatParam, DateParam,
		LocalDateTimeParam, LongParam, SetParam {

	String name();
	
    Object value();

    default String getName() {
        return name();
    }

    default Object getValue() {
        return value();
    }
    
	Class<?> getType();
	
	

}
