package com.kerneldc.metarbatch.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.kerneldc.metarbatch.exception.ApplicationRuntimeException;

@Configuration
public class DatabaseConfig {

	@Bean
	@Primary
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean
	@BatchDataSource
	@ConfigurationProperties(prefix = "spring-batch.datasource")
	public DataSource batchDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean
	public DataSourceInitializer batchDataSourceInitializer(@Qualifier("batchDataSource") DataSource batchDataSource) {
		if (! /* not */ isSchemaAlreadyInitialized(batchDataSource)) {
			ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
			populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-h2.sql"));
			DataSourceInitializer initializer = new DataSourceInitializer();
			initializer.setDataSource(batchDataSource);
			initializer.setDatabasePopulator(populator);
			return initializer;
		}
		return null;
	}
	
	@Bean
	@Primary
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

	@Bean
	@Qualifier("batchJdbcTemplate")
    public JdbcTemplate batchJdbcTemplate(@Qualifier("batchDataSource") DataSource batchDataSource) {
        return new JdbcTemplate(batchDataSource);
    }
	
    private boolean isSchemaAlreadyInitialized(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            ResultSet rs = connection.getMetaData().getTables(null, null, "BATCH_JOB_INSTANCE", null);
            return rs.next(); // table exists
        } catch (SQLException e) {
            throw new ApplicationRuntimeException("Failed to check if Spring Batch schema is initialized", e);
        }
    }
}
