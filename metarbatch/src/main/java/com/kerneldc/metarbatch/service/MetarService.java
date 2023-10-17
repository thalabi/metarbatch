package com.kerneldc.metarbatch.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Methods in this class are retried using aop and com.noaaServerSpringBatch.listener.RetryLogger
 *
 */
@Service
@Slf4j
public class MetarService {
	
	private static final String DEFAULT_WORK_DIR = "MetarBatchWork";
	private static final String LOCAL_DATE_TIME_FORMAT = "uuuuMMdd-HHmmss";
	private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT);
	
	@Value("${download.retry.max.attempts}")
	private int downloadRetryMaxAttempts;
	@Value("${download.retry.delay}")
	private int downloadRetryDelay;
	
	@Retryable(value = IOException.class, maxAttemptsExpression = "${download.retry.max.attempts}", backoff = @Backoff(delayExpression = "${download.retry.delay}"))
	public GZIPInputStream downloadMetarZipFile(String inputResourceString) throws IOException {
		
		LOGGER.info("inputResourceString: {}", inputResourceString);
		
		var inputResource = getResourceFromString(inputResourceString);
		
		GZIPInputStream metarZipInputStream = null;
		try {
			metarZipInputStream = new GZIPInputStream(new BufferedInputStream(inputResource.getInputStream()));
		} catch (IOException ioException) {
			var retryCount = RetrySynchronizationManager.getContext().getRetryCount();
			if (retryCount < downloadRetryMaxAttempts-1) { // retryCount is zero based 
				LOGGER.error("Failed to download file [{}], retrying in {} minutes ({}/{} attempts) ... ", inputResource.toString(), downloadRetryDelay / (60 * 1000f), retryCount+1, downloadRetryMaxAttempts);
			} else {
				LOGGER.error("Failed to download file [{}], exiting ({}/{} attempts) ... ", inputResource.toString(), retryCount+1, downloadRetryMaxAttempts);
			}
			throw ioException;
		}
		return metarZipInputStream;
	}

	public Path unzipMetarFile(GZIPInputStream metarZipInputStream, String workDirectory, String metarFile, LocalDateTime jobTimestamp) throws IOException {
		
		var effectiveWorkDirectory = getEffectiveWorkDirectory (workDirectory);
		LOGGER.info("effectiveWorkDirectory: [{}]", effectiveWorkDirectory);
		
		createDirectoryIfNotExist(effectiveWorkDirectory);
		
		var effectiveFileName = metarFile + "-" + jobTimestamp.format(LOCAL_DATE_TIME_FORMATTER);
		var metarFilePath = Paths.get(effectiveWorkDirectory, effectiveFileName);
		Files.copy(metarZipInputStream, metarFilePath, StandardCopyOption.REPLACE_EXISTING);
		
		return metarFilePath;
	}
		
	private String getEffectiveWorkDirectory (String workDirectory) {
		return StringUtils.isEmpty(workDirectory) ? System.getProperty("java.io.tmpdir")+File.separator+DEFAULT_WORK_DIR : workDirectory;
	}
	
	private void createDirectoryIfNotExist (String directory) throws IOException {
		var directoryPath = Paths.get(directory);
		if (Files.notExists(directoryPath)) {
			LOGGER.info("Creating directory [{}]", directoryPath);
			Files.createDirectory(directoryPath);
		}
	}
	
	private Resource getResourceFromString(String inputResourceString) {
		var resourceLoader = new DefaultResourceLoader();
		return resourceLoader.getResource(inputResourceString);
	}
}
