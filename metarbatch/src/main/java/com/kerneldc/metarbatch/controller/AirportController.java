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

//	@GetMapping("/getAirportIdentfierNamesOld2")
//	public ResponseEntity<Set<AirportIdentfierName>> getAirportIdentfierNamesOld2() {
//    	LOGGER.info(AppConstants.LOG_BEGIN);
//		Set<AirportIdentfierName> stationIdNameSet = Set.of(new AirportIdentfierName("CYOO", "Oshawa"),
//				new AirportIdentfierName("CYYZ", "Pearson"), new AirportIdentfierName("CYPQ", "Peterborough"),
//				new AirportIdentfierName("CNF4", "Lindsay"), new AirportIdentfierName("OLBA", "Beirut"),
//				new AirportIdentfierName("MUVR", "Varadero"));
//		LOGGER.info("airportIdentfierNameSet.size(): [{}]", stationIdNameSet.size());
//    	LOGGER.info(AppConstants.LOG_END);
//    	return ResponseEntity.ok(stationIdNameSet);
//	}

	@GetMapping("/getAirportIdentfierNames")
	public ResponseEntity<Set<AirportIdentfierName>> getAirportIdentfierNames(@RequestParam @NotBlank String idOrName) {
    	LOGGER.info(AppConstants.LOG_BEGIN);
		LOGGER.info("idOrName: [{}]", idOrName);
		var airportIdentfierNameSet = airportService.lookupByIdOrName(idOrName);
		LOGGER.info("airportIdentfierNameSet.size(): [{}]", airportIdentfierNameSet.size());
    	LOGGER.info(AppConstants.LOG_END);
    	return ResponseEntity.ok(airportIdentfierNameSet);
	}	

}
