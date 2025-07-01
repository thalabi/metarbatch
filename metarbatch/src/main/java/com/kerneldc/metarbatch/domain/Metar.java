package com.kerneldc.metarbatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Metar {
	
	private String rawText;
	
	@EmbeddedId
	private MetarPk metarPk;
	
    private Float latitude;
    private Float longitude;
    @Column(name="temp_c")
    private Float tempC;
    @Column(name="dewpoint_c")
    private Float dewpointC;
    private Integer windDirDegrees;
    private Integer windSpeedKt;
    private Integer windGustKt;
    private Float visibilityStatuteMi;
    private Float altimInHg;
    private Float seaLevelPressureMb;
    
	private String corrected;
	private String auto;
	private String autoStation;
	private String maintenanceIndicatorOn;
	private String noSignal;
	private String lightningSensorOff;
	private String freezingRainSensorOff;
	private String presentWeatherSensorOff;
    
    private String wxString;
    
    @Column(name="sky_cover_1")
    private String skyCover1;
    @Column(name="cloud_base_ft_agl_1")
    private Integer cloudBaseFtAgl1;
    @Column(name="sky_cover_2")
    private String skyCover2;
    @Column(name="cloud_base_ft_agl_2")
    private Integer cloudBaseFtAgl2;
    @Column(name="sky_cover_3")
    private String skyCover3;
    @Column(name="cloud_base_ft_agl_3")
    private Integer cloudBaseFtAgl3;
    @Column(name="sky_cover_4")
    private String skyCover4;
    @Column(name="cloud_base_ft_agl_4")
    private Integer cloudBaseFtAgl4;
    
    //private List<SkyCondition> skyCondition;
    private String flightCategory;
    private Float threeHrPressureTendencyMb;
    @Column(name="maxt_c")
    private Float maxTC;
    @Column(name="mint_c")
    private Float minTC;
    @Column(name="maxt24hr_c")
    private Float maxT24HrC;
    @Column(name="mint24hr_c")
    private Float minT24HrC;
    private Float precipIn;
    @Column(name="pcp3hr_in")
    private Float pcp3HrIn;
    @Column(name="pcp6hr_in")
    private Float pcp6HrIn;
    @Column(name="pcp24hr_in")
    private Float pcp24HrIn;
    private Float snowIn;
    private Integer vertVisFt;
    private String metarType;
    @Column(name="elevation_m")
    private Float elevationM;
}
