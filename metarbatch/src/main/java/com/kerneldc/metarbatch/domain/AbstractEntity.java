package com.kerneldc.metarbatch.domain;

import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.builder.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class AbstractEntity {
	
	protected static final String OFFSET_DATE_TIME_FORMAT = "uuuu-MM-dd HH:mm:ss.SSSZ";
	protected static final String LOCAL_DATE_TIME_FORMAT = "uuuu-MM-dd HH:mm:ss.SSS";
	protected static final DateTimeFormatter OFFSET_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(OFFSET_DATE_TIME_FORMAT);
	protected static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT);;

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
}
