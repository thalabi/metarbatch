package com.kerneldc.metarbatch.exception;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ApplicationException extends Exception {

	private static final long serialVersionUID = 1L;
	private final List<String> messageList = new ArrayList<>();

	public ApplicationException() {
	}

	public ApplicationException(String message) {
		super(message);
		addMessage(message);
	}

	public ApplicationException(Throwable arg0) {
		super(arg0);
	}

	public ApplicationException(String message, Throwable arg1) {
		super(message, arg1);
		addMessage(message);
	}

	public ApplicationException(String message, Throwable arg1, boolean arg2, boolean arg3) {
		super(message, arg1, arg2, arg3);
		addMessage(message);
	}

	@Override
	public String getMessage() {
		return String.join(System.lineSeparator(), messageList);
	}

	public List<String> addMessage(String message) {
		if (StringUtils.isNotEmpty(message)) {
			messageList.add(message);
		}
		return messageList;
	}

	public List<String> getMessageList() {
		return messageList;
	}
}
