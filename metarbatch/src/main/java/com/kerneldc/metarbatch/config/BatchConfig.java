package com.kerneldc.metarbatch.config;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
//import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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
import org.springframework.transaction.PlatformTransactionManager;

import com.kerneldc.metarbatch.batch.AlreadyRunningNotificationTasklet;
import com.kerneldc.metarbatch.batch.DeleteFileTasklet;
import com.kerneldc.metarbatch.batch.DeleteMetarStageTasklet;
import com.kerneldc.metarbatch.batch.DownloadTasklet;
import com.kerneldc.metarbatch.batch.InsertMetarStageStepListener;
import com.kerneldc.metarbatch.batch.LookupRunningJobsTasklet;
import com.kerneldc.metarbatch.batch.MergeMetarTasklet;
import com.kerneldc.metarbatch.batch.MetarJobFailureListener;
import com.kerneldc.metarbatch.batch.MetarProcessor;
import com.kerneldc.metarbatch.batch.MultipleRunningJobsDecider;
import com.kerneldc.metarbatch.batch.TransformXmlTasklet;
import com.kerneldc.metarbatch.domain.MetarStage;
import com.kerneldc.metarbatch.repository.MetarStageRepository;
import com.kerneldc.metarbatch.service.EmailService;
import com.kerneldc.metarbatch.service.MetarService;
import com.kerneldc.metarbatch.xml.schema.metar.METAR;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableBatchProcessing(dataSourceRef = "batchDataSource")
@EnableRetry
@RequiredArgsConstructor
public class BatchConfig {
	
	private final JobExplorer jobExplorer;
	private final JobRepository jobRepository;
	private final JdbcTemplate jdbcTemplate;
	private final EmailService emailService;
	private final MetarService metarService;
	private final PlatformTransactionManager transactionManage;
	private final MetarStageRepository metarStageRepository;
	private final EntityManagerFactory entityManagerFactory;
	
	public static final String METAR_JOB = "MetarJob";

	@Bean
	public DefaultJobParametersConverter defaultJobParametersConverter() {
		return new DefaultJobParametersConverter();
	}
	
	@Bean
	public Step lookupRunningJobsTasklet() {
		return new StepBuilder("LookupRunningJobsTasklet", jobRepository).tasklet(new LookupRunningJobsTasklet(jobExplorer), transactionManage).build();
	}
	@Bean
	public MultipleRunningJobsDecider multipleRunningJobsDecider() {
		return new MultipleRunningJobsDecider();
	}
	@Bean
	public Step alreadyRunningNotificationTasklet() {
		return new StepBuilder("AlreadyRunningNotificationTasklet", jobRepository).tasklet(new AlreadyRunningNotificationTasklet(emailService, defaultJobParametersConverter()), transactionManage).build();
	}
	@Bean
	public Step downloadStep() {
		return new StepBuilder("DownloadTasklet", jobRepository).tasklet(new DownloadTasklet(metarService), transactionManage).build();
	}
	@Bean
	public Step transformXmlStep() {
		return new StepBuilder("TransformXmlTasklet", jobRepository).tasklet(new TransformXmlTasklet(), transactionManage).build();
	}

	@Bean
	public Step deleteMetarStageTaskletlStep() {
		return new StepBuilder("DeleteMetarStageTasklet", jobRepository).tasklet(new DeleteMetarStageTasklet(metarStageRepository), transactionManage).build();
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
		return new StepBuilder("InsertMetarStage", jobRepository)
		.<METAR, MetarStage>chunk(1000, transactionManage)
		.reader(metarListItemReader)
		.processor(metarProcessor())
		.writer(metarStageWriter())
		.listener(new InsertMetarStageStepListener())
		.build();
	}
	// insertMetarStageStep end

	@Bean
	public Step mergeMetarStep() {
		return new StepBuilder("MergeMetarTasklet", jobRepository).tasklet(new MergeMetarTasklet(jdbcTemplate), transactionManage).build();
	}

	@Bean
	public Step deleteFileStep() {
		return new StepBuilder("DeleteFileTasklet", jobRepository).tasklet(new DeleteFileTasklet(), transactionManage).build();
	}
	
	@Value("${metar.max.attempts.to.abandon:3}")
	private int maxAttemptsToAbandon;

	@Bean
	public Job metarJob(Step insertMetarStageStep) {
		return new JobBuilder(METAR_JOB, jobRepository)
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
				.listener(new MetarJobFailureListener(emailService, jobExplorer, jobRepository, maxAttemptsToAbandon, defaultJobParametersConverter()))
				.build();
	}

}
