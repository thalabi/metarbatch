package com.kerneldc.metarbatch.controller;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.metarbatch.AppConstants;
import com.kerneldc.metarbatch.domain.Metar;
import com.kerneldc.metarbatch.repository.MetarRepository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/metarQueryController")
@RequiredArgsConstructor
@Slf4j
public class MetarQueryController {

	private final MetarRepository metarRepository; 
	
	@GetMapping("/getListForLatestNoOfObservations")
	public ResponseEntity<List<Metar>> getListForLatestNoOfObservations(@RequestParam @NotBlank String stationIds, @RequestParam @Positive int noOfObservations) {
    	LOGGER.info(AppConstants.LOG_BEGIN);
		LOGGER.info("stationIds: [{}], noOfObservations: [{}]", stationIds, noOfObservations);
		var stationIdList = Arrays.asList(stationIds.toUpperCase().split(","));
		var metarList = metarRepository.findLatestNoOfObservations(stationIdList, noOfObservations);
    	LOGGER.info(AppConstants.LOG_END);
    	return ResponseEntity.ok(metarList);
	}

	@GetMapping("/getMetarListInObervationTimeRange")
	public ResponseEntity<List<Metar>> getMetarListInObervationTimeRange(@RequestParam @NotBlank String stationIds,
			@RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fromObservationTime,
			@RequestParam @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime toObservationTime) {
    	LOGGER.info(AppConstants.LOG_BEGIN);
		LOGGER.info("stationIds: [{}], fromObservationTime: [{}], toObservationTime: [{}]", stationIds, fromObservationTime, toObservationTime);
		var stationIdList = Arrays.asList(stationIds.toUpperCase().split(","));
		var metarList = metarRepository.findBetweenObservationTimes(stationIdList, fromObservationTime, toObservationTime);
    	LOGGER.info(AppConstants.LOG_END);
    	return ResponseEntity.ok(metarList);
	}

}
