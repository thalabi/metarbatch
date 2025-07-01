package com.kerneldc.metarbatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kerneldc.metarbatch.domain.MetarPk;
import com.kerneldc.metarbatch.domain.MetarStage;

public interface MetarStageRepository extends JpaRepository<MetarStage, MetarPk> {
}
