package com.kerneldc.metarbatch.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kerneldc.metarbatch.domain.Metar;
import com.kerneldc.metarbatch.domain.MetarPk;

public interface MetarRepository extends JpaRepository<Metar, MetarPk> {

	//List<Metar> findLatestNoOfObservations2(Collection<String> stationIdCollection, int noOfObservations);
	
	@Query(value = """
		with ranked_metars as (
			select *, row_number() over (partition by station_id order by observation_time desc) as rn
			  from avwx.metar
		     where station_id in (:stationIdCollection)
		       and observation_time >= now() - INTERVAL '24 hours' -- restrict to last 24 hrs to use of partition pruning
		)
		select
			raw_text,
			station_id,
			observation_time,
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
		 from ranked_metars
		where rn <= :noOfObservations
		order by station_id, observation_time desc
		""", nativeQuery = true)
	List<Metar> findLatestNoOfObservations(Collection<String> stationIdCollection, int noOfObservations);
}
