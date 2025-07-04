package com.kerneldc.metarbatch.service.airport;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

import com.kerneldc.metarbatch.domain.remoteapicalllog.RemoteApiCallLog.RetryStatusEnum;
import com.kerneldc.metarbatch.exception.ApplicationException;
import com.kerneldc.metarbatch.repository.RemoteApiCallLogRepository;
import com.kerneldc.metarbatch.service.AbstractRemoteApiCallBase;
import com.kerneldc.metarbatch.service.EmailService;
import com.kerneldc.metarbatch.service.JwtTokenService;
import com.kerneldc.metarbatch.service.http.HttpRequestTypeEnum;
import com.kerneldc.metarbatch.service.http.HttpService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AirportInfoLoader extends AbstractRemoteApiCallBase {

	private final AirportService airportService;
	private final EmailService emailService;
	
	public AirportInfoLoader(RemoteApiCallLogRepository remoteApiCallLogRepository,
		JwtTokenService jwtTokenService, HttpService httpService, AirportService airportService, EmailService emailService) {
		super(remoteApiCallLogRepository, jwtTokenService, httpService);
		this.airportService = airportService;
		this.emailService = emailService;
	}

	@Retryable(retryFor = ApplicationException.class,
	        maxAttemptsExpression = "${remote.api.call.retry.max.attempts}",
	        backoff =
	        	@Backoff(delayExpression = "${remote.api.call.retry.delay}",
	        		multiplierExpression = "${remote.api.call.retry.multiplier}",
	        		maxDelayExpression = "${remote.api.call.retry.max.delay}") // retry after 30 sec, 1 min, 2 min, 4 min, ... 8192 min(5.68 days)
	)
//	listeners = {"loggingRetryListener"}
	public void loadRemotely(LoadAirportInfoEvent loadAirportInfoEvent) throws ApplicationException {
		var remoteApiCall = loadAirportInfoEvent.getRemoteApiCall();
		var retryCount = RetrySynchronizationManager.getContext().getRetryCount();
		var nextDelay = delay * Math.pow(multiplier, retryCount);
		
		LOGGER.info("retryCount [{}] maxAttempts [{}], delay [{}], multiplier [{}] nextDelay [{}]", retryCount,
				maxAttempts, delay, multiplier, nextDelay);
		
		try {
			
			var jwt = jwtTokenService.getJwtToken();
			
			var returnParams = httpService.processRequest(HttpRequestTypeEnum.AIRPORT_INFO, jwt);
			@SuppressWarnings("unchecked")
			Set<AirportIdentfierName> airportInfoSet = returnParams.get("airportInfoSet", Set.class);
			
			airportService.setAirportInfoMap(
					airportInfoSet.stream().collect(Collectors.toMap(AirportIdentfierName::identifier, AirportIdentfierName::name))
					);
			
			LOGGER.info("Loaded [{}] airport information", airportInfoSet.size());
			writeLog(remoteApiCall, retryCount + 1,
						(retryCount == 0 ? RetryStatusEnum.SUCCESS : RetryStatusEnum.RETRY_SUCCESS), null, 0);
			// send success email if call succeeded after retrying
			if (retryCount != 0) {
				LOGGER.info("Sending success after retrying email");
				emailService.sendRemoteApiSuccessAfterRetryEmail(HttpRequestTypeEnum.AIRPORT_INFO, retryCount);
			}
		} catch (ApplicationException applicationException) {
			writeLog(remoteApiCall, retryCount + 1, RetryStatusEnum.RETRY, applicationException, nextDelay);
			LOGGER.error(applicationException.getMessage());
			// send failure email on failure
			if (retryCount == 0) { // first time call fails
				LOGGER.info("Sending failure email");
				emailService.sendRemoteApiFailureEmail(HttpRequestTypeEnum.AIRPORT_INFO, applicationException);
			}
			throw applicationException;
		}
	}
	
	@Recover
	public void recover(Exception exception, LoadAirportInfoEvent loadAirportInfoEvent) {
		writeLog(loadAirportInfoEvent.getRemoteApiCall(), 0, RetryStatusEnum.GIVE_UP, null, 0);
		LOGGER.error("FlightLogPendingNotifier failed");
	}
}
