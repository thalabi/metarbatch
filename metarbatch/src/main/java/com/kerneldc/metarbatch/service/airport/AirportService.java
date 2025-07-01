package com.kerneldc.metarbatch.service.airport;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.kerneldc.metarbatch.domain.remoteapicall.RemoteApiCall;
import com.kerneldc.metarbatch.domain.remoteapicalllog.RemoteApiCallLog;
import com.kerneldc.metarbatch.domain.remoteapicalllog.RemoteApiCallLog.RetryStatusEnum;
import com.kerneldc.metarbatch.repository.RemoteApiCallLogRepository;
import com.kerneldc.metarbatch.repository.RemoteApiCallRepository;
import com.kerneldc.metarbatch.service.HttpService.RequestTypeEnum;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AirportService {
	
	private final RemoteApiCallLogRepository remoteApiCallLogRepository;
	private final RemoteApiCallRepository remoteApiCallRepository;
	private final ApplicationEventPublisher eventPublisher;
	
	@Setter
	private Set<String> identifierSet = new HashSet<>();
	
	@Setter
	private Map<String, String> airportInfoMap = new HashMap<>(); // key is airport identifier, value is airport name
	
	public void loadAirportInfoFromExternalApi() {

		// publish event to trigger remote api call
		var now = OffsetDateTime.now();
		
		var remoteApiCall = new RemoteApiCall();
		remoteApiCall.setRequest(RequestTypeEnum.AIRPORT_INFO);
		remoteApiCall.setTimestamp(now);
		remoteApiCallRepository.save(remoteApiCall);
		
		var remoteApiCallLog = new RemoteApiCallLog();
		remoteApiCallLog.setStatus(RetryStatusEnum.NEVER_ATTEMPTED);
		remoteApiCallLog.setRemoteApiCall(remoteApiCall);
		remoteApiCallLog.setTimestamp(now);
		remoteApiCallLogRepository.save(remoteApiCallLog);
		
		var loadAirportInfoEvent = new LoadAirportInfoEvent(this, remoteApiCall);
		eventPublisher.publishEvent(loadAirportInfoEvent);
	}

	public void refreshAirportInfoFromExternalApi() {
		LOGGER.info("Clearing airportInfoMap");
		airportInfoMap.clear();
		
		loadAirportInfoFromExternalApi();
	}
	
//	public Boolean isIdentifierValid(String identifier) throws ApplicationException {
//		if (CollectionUtils.isEmpty(identifierSet)) {
//			var message = "identifierSet is empty, possible error loading set from external api"; 
//			LOGGER.error(message);
//			throw new ApplicationException(message);
//		}
//		return identifierSet.contains(identifier);
//	}

}
