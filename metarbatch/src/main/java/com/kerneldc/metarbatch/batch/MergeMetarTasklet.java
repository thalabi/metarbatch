package com.kerneldc.metarbatch.batch;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class MergeMetarTasklet implements Tasklet {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
		var rowCount = jdbcTemplate.update("""
merge into metar d
		using (
		  select
		    raw_text,
		    station_id,
		    to_date(replace(replace(observation_time,'T',' '), 'Z',' '),'yyyy-mm-dd hh24:mi:ss ') observation_time,
		    latitude,
		    longitude,
		    temp_c,
		    dewpoint_c,
		    wind_dir_degrees,
		    wind_speed_kt,
		    wind_gust_kt,
		    visibility_statute_mi,
		    altim_in_hg,
		    sea_level_pressure_mb,
		    corrected,
		    auto,
		    auto_station,
		    maintenance_indicator_on,
		    no_signal,
		    lightning_sensor_off,
		    freezing_rain_sensor_off,
		    present_weather_sensor_off,
		    wx_string,
		    sky_cover_1,
		    cloud_base_ft_agl_1,
		    sky_cover_2,
		    cloud_base_ft_agl_2,
		    sky_cover_3,
		    cloud_base_ft_agl_3,
		    sky_cover_4,
		    cloud_base_ft_agl_4,
		    flight_category,
		    three_hr_pressure_tendency_mb,
		    maxt_c,
		    mint_c,
		    maxt24hr_c,
		    mint24hr_c,
		    precip_in,
		    pcp3hr_in,
		    pcp6hr_in,
		    pcp24hr_in,
		    snow_in,
		    vert_vis_ft,
		    metar_type,
		    elevation_m
		  from (/* remove duplicates */ select m.*, row_number() over (partition by m.station_id, m.observation_time order by m.station_id) as row_number from metar_stage m) where row_number=1) s
		    on (d.station_id = s.station_id and
		        d.observation_time = s.observation_time)
		when not matched
		then
		insert (
		  raw_text, station_id, observation_time,
		  latitude, longitude, temp_c,
		  dewpoint_c, wind_dir_degrees, wind_speed_kt,
		  wind_gust_kt, visibility_statute_mi, altim_in_hg,
		  sea_level_pressure_mb, corrected, auto,
		  auto_station, maintenance_indicator_on, no_signal,
		  lightning_sensor_off, freezing_rain_sensor_off, present_weather_sensor_off,
		  wx_string, sky_cover_1, cloud_base_ft_agl_1,
		  sky_cover_2, cloud_base_ft_agl_2, sky_cover_3,
		  cloud_base_ft_agl_3, sky_cover_4, cloud_base_ft_agl_4,
		  flight_category, three_hr_pressure_tendency_mb, maxt_c,
		  mint_c, maxt24hr_c, mint24hr_c,
		  precip_in, pcp3hr_in, pcp6hr_in,
		  pcp24hr_in, snow_in, vert_vis_ft,
		  metar_type, elevation_m)
		values (
		  s.raw_text, s.station_id, s.observation_time,
		  s.latitude, s.longitude, s.temp_c,
		  s.dewpoint_c, s.wind_dir_degrees, s.wind_speed_kt,
		  s.wind_gust_kt, s.visibility_statute_mi, s.altim_in_hg,
		  s.sea_level_pressure_mb, s.corrected, s.auto,
		  s.auto_station, s.maintenance_indicator_on, s.no_signal,
		  s.lightning_sensor_off, s.freezing_rain_sensor_off, s.present_weather_sensor_off,
		  s.wx_string, s.sky_cover_1, s.cloud_base_ft_agl_1,
		  s.sky_cover_2, s.cloud_base_ft_agl_2, s.sky_cover_3,
		  s.cloud_base_ft_agl_3, s.sky_cover_4, s.cloud_base_ft_agl_4,
		  s.flight_category, s.three_hr_pressure_tendency_mb, s.maxt_c,
		  s.mint_c, s.maxt24hr_c, s.mint24hr_c,
		  s.precip_in, s.pcp3hr_in, s.pcp6hr_in,
		  s.pcp24hr_in, s.snow_in, s.vert_vis_ft,
		  s.metar_type, s.elevation_m)
		""");

		LOGGER.info("Number of rows inserted/updated: [{}]", rowCount);
		return RepeatStatus.FINISHED;
	}
}
