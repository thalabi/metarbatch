package com.kerneldc.metarbatch.service;

import java.sql.ResultSet;
import java.text.MessageFormat;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kerneldc.metarbatch.AppConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetarPartitionService {

	private static final String PARTITION_NAME_TEMPLATE = "metar_y{0,number,0000}_m{1,number,00}";
	private static final String PARTITION_QUERY_SQL = """
			select c.relname as partition_name
			  from pg_inherits i
			  join pg_class c on i.inhrelid = c.oid
			  join pg_class p on i.inhparent = p.oid
			 where p.relname = 'metar'
			   and c.relname = :partitionName
			""";
	private static final String CREATE_PARTITION_SQL_TEMPLATE = """
			create table %s partition of metar for values from ('%s') to ('%s')
			""";
	
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final JdbcTemplate jdbcTemplate;
	
	private String thisMonthPartitionName() {
		return getPartitionName(0);
	}
	private String nextMonthPartitionName() {
		return getPartitionName(1);
	}
	private String getPartitionName(int monthsToAdd) {
		var nextMonth = LocalDate.now().plusMonths(monthsToAdd);
		return MessageFormat.format(PARTITION_NAME_TEMPLATE, nextMonth.getYear(), nextMonth.getMonthValue());
	}
	
	private boolean thisMonthPartitionExists() {
		return partitionExists(0);
	}
	private boolean nextMonthPartitionExists() {
		return partitionExists(1);
	}
	private boolean partitionExists(int monthsToAdd) {
		var partitionName = getPartitionName(monthsToAdd);
		LOGGER.info("Checking that {} month partition [{}] exists ...", (monthsToAdd == 0 ? "this" : "next"), partitionName);
		var params = new MapSqlParameterSource().addValue("partitionName", partitionName);
		boolean exists = namedParameterJdbcTemplate.query(PARTITION_QUERY_SQL, params, ResultSet::next); // true if exists
		LOGGER.info("Partition [{}] {}", partitionName, (exists ? "exists" : "does not exist"));
		return exists;
	}

	public void createThisMonthPartition() {
		if (thisMonthPartitionExists()) {
			return;
		}
		var now = LocalDate.now();
		var month1 = AppConstants.DATE_FORMATER_YYYY_MM_DD.format(now.withDayOfMonth(1));
		var month2 = AppConstants.DATE_FORMATER_YYYY_MM_DD.format(now.plusMonths(1).withDayOfMonth(1));
		var sql = String.format(CREATE_PARTITION_SQL_TEMPLATE, thisMonthPartitionName(), month1, month2);
		LOGGER.info("Creating this month partition: [{}]", sql);
		jdbcTemplate.execute(sql);
	}
	
	@Scheduled(cron = "${create.partition.schedule.cron.expression}")
	public void createNextMonthPartition() {
		if (nextMonthPartitionExists()) {
			return;
		}
		var now = LocalDate.now();
		var month1 = AppConstants.DATE_FORMATER_YYYY_MM_DD.format(now.plusMonths(1).withDayOfMonth(1));
		var month2 = AppConstants.DATE_FORMATER_YYYY_MM_DD.format(now.plusMonths(2).withDayOfMonth(1));
		var sql = String.format(CREATE_PARTITION_SQL_TEMPLATE, nextMonthPartitionName(), month1, month2);
		LOGGER.info("Creating next month partition: [{}]", sql);
		jdbcTemplate.execute(sql);
	}

}
