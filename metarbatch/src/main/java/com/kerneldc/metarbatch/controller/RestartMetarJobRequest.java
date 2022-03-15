package com.kerneldc.metarbatch.controller;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RestartMetarJobRequest {

	private Long jobExecutionId;
}
