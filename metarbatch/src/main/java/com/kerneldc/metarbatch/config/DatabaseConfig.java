package com.kerneldc.metarbatch.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

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
	@Primary
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

	@Bean
	@Qualifier("batchJdbcTemplate")
    public JdbcTemplate batchJdbcTemplate(@Qualifier("batchDataSource") DataSource batchDataSource) {
        return new JdbcTemplate(batchDataSource);
    }
}
