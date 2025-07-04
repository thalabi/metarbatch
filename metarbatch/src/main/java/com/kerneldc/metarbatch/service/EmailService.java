package com.kerneldc.metarbatch.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.batch.core.JobParameter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.kerneldc.metarbatch.MetarJobManager;
import com.kerneldc.metarbatch.exception.ApplicationException;
import com.kerneldc.metarbatch.service.http.HttpRequestTypeEnum;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
	
	@Value("${application.email.emailNotificationFrom}")
	private String emailNotificationFrom;
	@Value("${application.email.emailNotificationTo}")
	private String metarJobNotificationTo;

	private static final String METAR_JOB_FAILURE_NOTIFICATION_SUBJECT = "Metar Job Failure";
	private static final String METAR_JOB_ALREADY_RUNNING_NOTIFICATION_SUBJECT = "Metar Job Already Running";
	private static final String METAR_JOB_RESTART_FAILURE_NOTIFICATION_SUBJECT = "Metar Job Restart Failure";
	private static final String METAR_JOB_SET_TO_ABANDONED_NOTIFICATION_SUBJECT = "Metar Job set to 'ABANDONED'";
	private static final String CREATED_METAR_TABLE_PARTITION_NOTIFICATION_SUBJECT = "Create Metar Table Partition";
	private static final String RERMOTE_API_FAILURE_SUBJECT = "Remote API Failure";
	private static final String RERMOTE_API_SUCCESS_AFTER_RETRY_SUBJECT = "Remote API Success After Retry";
	private static final String METAR_JOB_FAILURE_TEMPLATE = "metarJobFailure.ftlh";
	private static final String METAR_JOB_ALREADY_RUNNING_TEMPLATE = "metarJobAlreadyRunning.ftlh";
	private static final String METAR_JOB_RESTART_FAILURE_TEMPLATE = "metarJobRestartFailure.ftlh";
	private static final String METAR_JOB_SET_TO_ABANDONED_TEMPLATE = "metarJobSetToAbandoned.ftlh";
	private static final String CREATED_METAR_TABLE_PARTITION_TEMPLATE = "createdMetarPartition.ftlh";
	private static final String REMOTE_API_FAILURE_TEMPLATE = "remoteApiFailure.ftlh";
	private static final String REMOTE_API_SUCCESS_AFTER_FAILURE_TEMPLATE = "remoteApiSuccessAfterRetry.ftlh";

	private final JavaMailSender javaMailSender;
	private final Configuration freeMarkerConfiguration;
	
	public void sendMetarJobFailure(Map<String, JobParameter<?>> jobParametersMap, List<String> stacktraceList) throws ApplicationException {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		try {
			mimeMessageHelper.setFrom(emailNotificationFrom);
			mimeMessageHelper.setTo(InternetAddress.parse(metarJobNotificationTo));
			mimeMessageHelper.setSubject(METAR_JOB_FAILURE_NOTIFICATION_SUBJECT);
			mimeMessageHelper.setText(processMetarJobFailureTemplate(jobParametersMap, stacktraceList), true);
			javaMailSender.send(mimeMessage);
		} catch (MessagingException | IOException | TemplateException e) {
			var message = "Exception while sending Metar Job Failure email."; 
			LOGGER.error(message, e);
			throw new ApplicationException(message + " (" + e.getMessage() + ")");
			
		}
		LOGGER.info("Metar job failure notification email sent to: {}", metarJobNotificationTo);
	}
	
	public void sendMetarJobAlreadyRunning(Properties currentJobParametersMap, Properties runningJobParametersMap) throws ApplicationException {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		try {
			mimeMessageHelper.setFrom(emailNotificationFrom);
			mimeMessageHelper.setTo(InternetAddress.parse(metarJobNotificationTo));
			mimeMessageHelper.setSubject(METAR_JOB_ALREADY_RUNNING_NOTIFICATION_SUBJECT);
			mimeMessageHelper.setText(processMetarJobAlreadyRunningTemplate(currentJobParametersMap, runningJobParametersMap), true);
			javaMailSender.send(mimeMessage);
		} catch (MessagingException | IOException | TemplateException e) {
			var message = "Exception while sending Metar Job Failure email."; 
			LOGGER.error(message, e);
			throw new ApplicationException(message + " (" + e.getMessage() + ")");
			
		}
		LOGGER.info("Metar job already running notification email sent to: {}", metarJobNotificationTo);
	}

	public void sendMetarJobRestartFailure(Long jobExecutionId, String stacktrace) throws ApplicationException {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		try {
			mimeMessageHelper.setFrom(emailNotificationFrom);
			mimeMessageHelper.setTo(InternetAddress.parse(metarJobNotificationTo));
			mimeMessageHelper.setSubject(METAR_JOB_RESTART_FAILURE_NOTIFICATION_SUBJECT);
			mimeMessageHelper.setText(processMetarJobRestartFailureTemplate(jobExecutionId, stacktrace), true);
			javaMailSender.send(mimeMessage);
		} catch (MessagingException | IOException | TemplateException e) {
			var message = "Exception while sending Metar Job Restart Failure email."; 
			LOGGER.error(message, e);
			throw new ApplicationException(message + " (" + e.getMessage() + ")");
			
		}
		LOGGER.info("Metar job restart failure notification email sent to: {}", metarJobNotificationTo);
	}

	public void sendMetarJobSetToAbandoned(Long jobExecutionId) throws ApplicationException {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		try {
			mimeMessageHelper.setFrom(emailNotificationFrom);
			mimeMessageHelper.setTo(InternetAddress.parse(metarJobNotificationTo));
			mimeMessageHelper.setSubject(METAR_JOB_SET_TO_ABANDONED_NOTIFICATION_SUBJECT);
			mimeMessageHelper.setText(processMetarJobSetToAbandonedTemplate(jobExecutionId), true);
			javaMailSender.send(mimeMessage);
		} catch (MessagingException | IOException | TemplateException e) {
			var message = "Exception while sending Metar Job Restart Failure email."; 
			LOGGER.error(message, e);
			throw new ApplicationException(message + " (" + e.getMessage() + ")");
			
		}
		LOGGER.info("Metar job 'set to ABANDONED' notification email sent to: {}", metarJobNotificationTo);
	}
	public void sendCreatedMetarTablePartition(YearMonth yearMonth) throws ApplicationException {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		try {
			mimeMessageHelper.setFrom(emailNotificationFrom);
			mimeMessageHelper.setTo(InternetAddress.parse(metarJobNotificationTo));
			mimeMessageHelper.setSubject(CREATED_METAR_TABLE_PARTITION_NOTIFICATION_SUBJECT);
			mimeMessageHelper.setText(processCreatedMetarTablePartitionTemplate(yearMonth), true);
			javaMailSender.send(mimeMessage);
		} catch (MessagingException | IOException | TemplateException e) {
			var message = "Exception while sending Metar Job Restart Failure email."; 
			LOGGER.error(message, e);
			throw new ApplicationException(message + " (" + e.getMessage() + ")");
			
		}
		LOGGER.info("Metar job 'set to ABANDONED' notification email sent to: {}", metarJobNotificationTo);
	}
	
	public void sendRemoteApiFailureEmail(HttpRequestTypeEnum httpRequestTypeEnum, ApplicationException loadingFromExternalApiException) {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		try {
			mimeMessageHelper.setFrom(emailNotificationFrom);
			mimeMessageHelper.setTo(InternetAddress.parse(metarJobNotificationTo));
			mimeMessageHelper.setSubject(RERMOTE_API_FAILURE_SUBJECT);
			mimeMessageHelper.setText(processRemoteApiFailureTemplate(httpRequestTypeEnum, loadingFromExternalApiException), true);
			javaMailSender.send(mimeMessage);
			LOGGER.info("Sent remote api failure email to: {}", metarJobNotificationTo);
		} catch (MessagingException | IOException | TemplateException e) {
			var message = "Exception while sending failure email."; 
			LOGGER.error(message, e);
			LOGGER.info("Failed to send remote api failure email to: {}", metarJobNotificationTo);
		}
	}
	public void sendRemoteApiSuccessAfterRetryEmail(HttpRequestTypeEnum httpRequestTypeEnum, int retryCount) {
		var mimeMessage = javaMailSender.createMimeMessage();
		var mimeMessageHelper = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
		try {
			mimeMessageHelper.setFrom(emailNotificationFrom);
			mimeMessageHelper.setTo(InternetAddress.parse(metarJobNotificationTo));
			mimeMessageHelper.setSubject(RERMOTE_API_SUCCESS_AFTER_RETRY_SUBJECT);
			mimeMessageHelper.setText(processRemoteApiSuccessAfterRetryTemplate(httpRequestTypeEnum, retryCount), true);
			javaMailSender.send(mimeMessage);
			LOGGER.info("Sent reomte api success after retry email to: {}", metarJobNotificationTo);
		} catch (MessagingException | IOException | TemplateException e) {
			var message = "Exception while sending failure email."; 
			LOGGER.error(message, e);
			LOGGER.info("Failed to send reomte api success after retry email to: {}", metarJobNotificationTo);
		}
	}

	private String processMetarJobFailureTemplate(Map<String, JobParameter<?>> jobParametersMap, List<String> stacktraceList) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("jobParametersMap", jobParametersMap);
		templateModelMap.put("stacktraceList", stacktraceList);
		templateModelMap.put("jobTimestampFieldName", MetarJobManager.JOB_TIMESTAMP);
		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(METAR_JOB_FAILURE_TEMPLATE), templateModelMap);
	}
	private String processMetarJobAlreadyRunningTemplate(Properties currentJobParametersMap, Properties runningJobParametersMap) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("currentJobParametersMap", currentJobParametersMap);
		templateModelMap.put("runningJobParametersMap", runningJobParametersMap);
		templateModelMap.put("jobTimestampFieldName", MetarJobManager.JOB_TIMESTAMP);
		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(METAR_JOB_ALREADY_RUNNING_TEMPLATE), templateModelMap);
	}
	private String processMetarJobRestartFailureTemplate(Long jobExecutionId, String stacktrace) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("jobExecutionId", jobExecutionId);
		templateModelMap.put("stacktrace", stacktrace);
		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(METAR_JOB_RESTART_FAILURE_TEMPLATE), templateModelMap);
	}
	
	private String processMetarJobSetToAbandonedTemplate(Long jobExecutionId) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("jobExecutionId", jobExecutionId);
		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(METAR_JOB_SET_TO_ABANDONED_TEMPLATE), templateModelMap);
	}
	
	private String processCreatedMetarTablePartitionTemplate(YearMonth yearMonth) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("year", yearMonth.getYear());
		templateModelMap.put("month", yearMonth.getMonthValue());
		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(CREATED_METAR_TABLE_PARTITION_TEMPLATE), templateModelMap);
	}
	private String processRemoteApiFailureTemplate(HttpRequestTypeEnum httpRequestTypeEnum, ApplicationException loadingFromExternalApiException) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("httpRequestTypeEnum", httpRequestTypeEnum);
		templateModelMap.put("loadingFromExternalApiException", loadingFromExternalApiException);
		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(REMOTE_API_FAILURE_TEMPLATE), templateModelMap);
	}
	private String processRemoteApiSuccessAfterRetryTemplate(HttpRequestTypeEnum httpRequestTypeEnum, int retryCount) throws IOException, TemplateException {
		Map<String, Object> templateModelMap = new HashMap<>();
		templateModelMap.put("httpRequestTypeEnum", httpRequestTypeEnum);
		templateModelMap.put("retryCount", retryCount);
		return FreeMarkerTemplateUtils.processTemplateIntoString(freeMarkerConfiguration.getTemplate(REMOTE_API_SUCCESS_AFTER_FAILURE_TEMPLATE), templateModelMap);
	}

}
