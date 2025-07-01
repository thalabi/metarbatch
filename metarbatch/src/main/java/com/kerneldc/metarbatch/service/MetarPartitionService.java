package com.kerneldc.metarbatch.service;

import java.sql.ResultSet;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kerneldc.metarbatch.AppConstants;
import com.kerneldc.metarbatch.exception.ApplicationException;

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
	private final EmailService emailService;
	
	private String getPartitionName(int monthsToAdd) {
		var nextMonth = LocalDate.now().plusMonths(monthsToAdd);
		return MessageFormat.format(PARTITION_NAME_TEMPLATE, nextMonth.getYear(), nextMonth.getMonthValue());
	}

	private boolean partitionExists(int monthsToAdd) {
		var partitionName = getPartitionName(monthsToAdd);
		LOGGER.info("Checking that {} month partition [{}] exists ...", (monthsToAdd == 0 ? "this" : "next"), partitionName);
		var params = new MapSqlParameterSource().addValue("partitionName", partitionName);
		boolean exists = namedParameterJdbcTemplate.query(PARTITION_QUERY_SQL, params, ResultSet::next); // true if exists
		LOGGER.info("Partition [{}] {}", partitionName, (exists ? "exists" : "does not exist"));
		return exists;
	}

	private YearMonth getYearMonth(int monthsToAdd) {
		var date = LocalDate.now().plusMonths(monthsToAdd);
		return YearMonth.from(date);
	}
	
	private void createPartition(int monthsToAdd) throws ApplicationException {
		if (partitionExists(monthsToAdd)) {
			return;
		}
		var now = LocalDate.now();
		var month1 = AppConstants.DATE_FORMATER_YYYY_MM_DD.format(now.plusMonths(monthsToAdd).withDayOfMonth(1));
		var month2 = AppConstants.DATE_FORMATER_YYYY_MM_DD.format(now.plusMonths(monthsToAdd+1L).withDayOfMonth(1));
		var sql = String.format(CREATE_PARTITION_SQL_TEMPLATE, getPartitionName(monthsToAdd), month1, month2);
		LOGGER.info("Creating {} month partition: [{}]", (monthsToAdd == 0 ? "this" : "next"), sql);
		jdbcTemplate.execute(sql);
		emailService.sendCreatedMetarTablePartition(getYearMonth(monthsToAdd));
	}

	public void createThisMonthPartition() throws ApplicationException {
		createPartition(0);
	}
	
	@Scheduled(cron = "${create.partition.schedule.cron.expression}")
	public void createNextMonthPartition() throws ApplicationException {
		createPartition(1);
	}
}
