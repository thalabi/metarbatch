package com.kerneldc.metarbatch.domain;

public interface ILogicallyKeyed {
	public static final String PROPERTY_LOGICAL_KEY = "logicalKey";
	public static final String COLUMN_LK = "lk";

	String getLogicalKey();
	void setLogicalKey(String logicalKey);

}
