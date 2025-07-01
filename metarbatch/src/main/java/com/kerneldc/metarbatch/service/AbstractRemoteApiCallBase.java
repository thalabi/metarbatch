package com.kerneldc.metarbatch.service;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;

import com.kerneldc.metarbatch.domain.remoteapicall.RemoteApiCall;
import com.kerneldc.metarbatch.domain.remoteapicalllog.RemoteApiCallLog;
import com.kerneldc.metarbatch.domain.remoteapicalllog.RemoteApiCallLog.RetryStatusEnum;
import com.kerneldc.metarbatch.repository.RemoteApiCallLogRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractRemoteApiCallBase {

	protected final RemoteApiCallLogRepository remoteApiCallLogRepository;
	protected final JwtTokenService jwtTokenService;
	protected final HttpService httpService;
	
	@Value("${remote.api.call.retry.max.attempts}")
	protected int maxAttempts;
	@Value("${remote.api.call.retry.delay}")
	protected long delay;
	@Value("${remote.api.call.retry.multiplier}")
	protected int multiplier;

	protected void writeLog(RemoteApiCall remoteApiCall, int attempt, RetryStatusEnum status, Exception exception, double nextDelay) {
		var now = OffsetDateTime.now();
		var remoteApiCallLog = new RemoteApiCallLog();
		remoteApiCallLog.setRemoteApiCall(remoteApiCall);
		if (attempt != 0) {
			remoteApiCallLog.setAttempt(attempt);
		}
		remoteApiCallLog.setStatus(status);
		if (exception != null) {
			remoteApiCallLog.setMessage(exception.getMessage());
			remoteApiCallLog.setStackTrace(ExceptionUtils.getStackTrace(exception));
		}
		remoteApiCallLog.setTimestamp(now);
		if (nextDelay != 0) {
			remoteApiCallLog.setNextRetryTime(now.plus(Duration.ofMillis((long)nextDelay)));
		}
		remoteApiCallLogRepository.save(remoteApiCallLog);
	}

}
