	package com.kerneldc.metarbatch.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

	private TimeUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static OffsetDateTime toOffsetDateTime(Instant instant) {
		return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
	}
	
	public static OffsetDateTime toOffsetDateTime(Date date) {
		return date == null ? null : toOffsetDateTime(date.toInstant());
	}

	public static OffsetDateTime toOffsetDateTime(Calendar calendar) {
		return calendar == null ? null : toOffsetDateTime(calendar.toInstant());
	}
	public static LocalDate toLocalDate(Instant instant) {
		return instant == null ? null : LocalDate.ofInstant(instant, ZoneId.systemDefault());
	}

	public static LocalDateTime toLocalDateTime(Instant instant) {
		return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}
	
	public static LocalDateTime toLocalDateTime(Date date) {
		return date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}
	
	public static Instant toInstant(LocalDate localDate) {
		return localDate == null ? null : localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
	}

	public static Date toDate(LocalDateTime localDateTime) {
		return localDateTime == null ? null :  Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
}
