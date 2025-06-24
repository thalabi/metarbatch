package com.kerneldc.metarbatch;

import java.time.format.DateTimeFormatter;

public class AppConstants {

	public static final String LOG_BEGIN = "Begin ...";
	public static final String LOG_END = "End ...";
	
	public static final DateTimeFormatter DATE_FORMATER_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private AppConstants() {
		throw new UnsupportedOperationException("This class cannot be instantiated.");
	}

}
