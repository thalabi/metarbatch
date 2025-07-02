package com.kerneldc.metarbatch.controller;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kerneldc.metarbatch.AppConstants;
import com.kerneldc.metarbatch.service.airport.AirportIdentfierName;
import com.kerneldc.metarbatch.service.airport.AirportService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/protected/airportController")
@RequiredArgsConstructor
@Slf4j
public class AirportController {

	private final AirportService airportService; 
	
	@GetMapping("/getAirportInfo")
	public ResponseEntity<Set<AirportIdentfierName>> getAirportInfo(@RequestParam @NotBlank String idOrName) {
    	LOGGER.info(AppConstants.LOG_BEGIN);
		LOGGER.info("idOrName: [{}]", idOrName);
		var airportIdentfierNameSet = airportService.lookupByIdOrName(idOrName);
    	LOGGER.info(AppConstants.LOG_END);
    	return ResponseEntity.ok(airportIdentfierNameSet);
	}
}
