package com.kerneldc.metarbatch.domain;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class MetarPk implements Serializable {

	private static final long serialVersionUID = 1L;

	private String stationId;
	private String observationTime;

}
