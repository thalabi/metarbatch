package com.kerneldc.metarbatch.domain;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class AbstractEntity {
	
	protected static final String OFFSET_DATE_TIME_FORMAT = "uuuu-MM-dd HH:mm:ss.SSSZ";
	protected static final String LOCAL_DATE_TIME_FORMAT = "uuuu-MM-dd HH:mm:ss.SSS";
	protected static final DateTimeFormatter OFFSET_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(OFFSET_DATE_TIME_FORMAT);
	// This is the same fprmat as produced by ISO_INSTANT except it displays milliseconds
	// Source from ISO_INSTANT declaration
	protected static final DateTimeFormatter OFFSET_DATE_TIME_UTC_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendInstant(3) // milliseconds
            .toFormatter();
	protected static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT);;

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public static final Function<AbstractEntity, Object> idExtractor = AbstractEntity::getId;

	abstract Long getId(); 
	
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
}
