package com.kerneldc.metarbatch.config;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.retry.annotation.EnableRetry;

import com.kerneldc.metarbatch.batch.AlreadyRunningNotificationTasklet;
import com.kerneldc.metarbatch.batch.DeleteFileTasklet;
import com.kerneldc.metarbatch.batch.DeleteMetarStageTasklet;
import com.kerneldc.metarbatch.batch.DownloadTasklet;
import com.kerneldc.metarbatch.batch.LookupRunningJobsTasklet;
import com.kerneldc.metarbatch.batch.MergeMetarTasklet;
import com.kerneldc.metarbatch.batch.MetarJobFailureListener;
import com.kerneldc.metarbatch.batch.MetarProcessor;
import com.kerneldc.metarbatch.batch.MultipleRunningJobsDecider;
import com.kerneldc.metarbatch.batch.RecordCountStepListener;
import com.kerneldc.metarbatch.batch.TransformXmlTasklet;
import com.kerneldc.metarbatch.domain.MetarStage;
import com.kerneldc.metarbatch.repository.MetarStageRepository;
import com.kerneldc.metarbatch.service.EmailService;
import com.kerneldc.metarbatch.service.MetarService;
import com.kerneldc.metarbatch.xml.schema.metar.METAR;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableBatchProcessing
@EnableRetry
@RequiredArgsConstructor
public class BatchConfig {
	
	private final JobExplorer jobExplorer;
	private final JobRegistry jobRegistry;
	private final JobBuilderFactory jobBuilderFactory;
	private final StepBuilderFactory stepBuilderFactory;
	private final JdbcTemplate jdbcTemplate;
	private final EmailService emailService;
	private final MetarService metarService;
	private final MetarStageRepository metarStageRepository;
	private final EntityManagerFactory entityManagerFactory;
	
	public static final String METAR_JOB = "MetarJob";
	
	// Needed for job restart
	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
	    JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
	    postProcessor.setJobRegistry(jobRegistry);
	    return postProcessor;
	}
	
	
	
	@Bean
	public Step lookupRunningJobsTasklet() {
		return stepBuilderFactory.get("LookupRunningJobsTasklet").tasklet(new LookupRunningJobsTasklet(jobExplorer)).build();
	}
	@Bean
	public MultipleRunningJobsDecider multipleRunningJobsDecider() {
		return new MultipleRunningJobsDecider();
	}
	@Bean
	public Step alreadyRunningNotificationTasklet() {
		return stepBuilderFactory.get("AlreadyRunningNotificationTasklet").tasklet(new AlreadyRunningNotificationTasklet(emailService)).build();
	}
	@Bean
	public Step downloadStep() {
		return stepBuilderFactory.get("DownloadTasklet").tasklet(new DownloadTasklet(metarService)).build();
	}
	@Bean
	public Step transformXmlStep() {
		return stepBuilderFactory.get("TransformXmlTasklet").tasklet(new TransformXmlTasklet()).build();
	}

	@Bean
	public Step deleteMetarStageTaskletlStep() {
		return stepBuilderFactory.get("DeleteMetarStageTasklet").tasklet(new DeleteMetarStageTasklet(metarStageRepository)).build();
	}

	// insertMetarStageStep begin
	@Bean
	@StepScope
	public ListItemReader<METAR> metarListItemReader(@Value("#{jobExecutionContext['metarList']}") List<METAR> metarList) {
		return new ListItemReader<>(metarList);
	}
	
	@Bean
	public ItemProcessor<METAR, MetarStage> metarProcessor() {
		return new MetarProcessor();
	}
	@Bean
	public JpaItemWriter<MetarStage> metarStageWriter() {
		return new JpaItemWriterBuilder<MetarStage>().entityManagerFactory(entityManagerFactory).build();
	}
	@Bean
	public Step insertMetarStageStep(ItemReader<METAR> metarListItemReader) {
		return 
				stepBuilderFactory.get("InsertMetarStage")
				.<METAR, MetarStage>chunk(1000)
				.reader(metarListItemReader)
				.processor(metarProcessor())
				.writer(metarStageWriter())
				.listener(new RecordCountStepListener())
				.build();
	}
	// insertMetarStageStep end

	@Bean
	public Step mergeMetarStep() {
		return stepBuilderFactory.get("MergeMetarTasklet").tasklet(new MergeMetarTasklet(jdbcTemplate)).build();
	}

	@Bean
	public Step deleteFileStep() {
		return stepBuilderFactory.get("DeleteFileTasklet").tasklet(new DeleteFileTasklet()).build();
	}
	
	@Bean
	public Job metarJob(Step insertMetarStageStep) {
		return jobBuilderFactory.get(METAR_JOB)
				.incrementer(new RunIdIncrementer())
				.start(lookupRunningJobsTasklet())
				.next(multipleRunningJobsDecider()).on(MultipleRunningJobsDecider.RUNNING)
					.to(alreadyRunningNotificationTasklet())
				.from(multipleRunningJobsDecider()).on(MultipleRunningJobsDecider.NOT_RUNNING)
					.to(downloadStep()).next(transformXmlStep())
					.next(deleteMetarStageTaskletlStep())
					.next(insertMetarStageStep)
					.next(mergeMetarStep()).next(deleteFileStep())
				.end()
				.listener(new MetarJobFailureListener(emailService))
				.build();
	}
}
