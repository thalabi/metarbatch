package com.kerneldc.metarbatch.batch;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.BeanUtils;

import com.kerneldc.metarbatch.domain.MetarStage;
import com.kerneldc.metarbatch.xml.schema.metar.METAR;

public class MetarProcessor implements ItemProcessor<METAR, MetarStage> {

	private static final String TRUE = "TRUE";
	private static final String Y = "Y";
	
	@Override
	public MetarStage process(METAR metar) throws Exception {
		var metarStage = new MetarStage();

		BeanUtils.copyProperties(metar, metarStage);

		if (metar.getQualityControlFlags() != null) {
			if (StringUtils.equals(metar.getQualityControlFlags().getCorrected(), TRUE)) {
				metarStage.setCorrected(Y);
			}
			if (StringUtils.equals(metar.getQualityControlFlags().getAuto(), TRUE)) {
				metarStage.setAuto(Y);
			}
			if (StringUtils.equals(metar.getQualityControlFlags().getAutoStation(), TRUE)) {
				metarStage.setAutoStation(Y);
			}
			if (StringUtils.equals(metar.getQualityControlFlags().getMaintenanceIndicatorOn(), TRUE)) {
				metarStage.setMaintenanceIndicatorOn(Y);
			}
			if (StringUtils.equals(metar.getQualityControlFlags().getNoSignal(), TRUE)) {
				metarStage.setNoSignal(Y);
			}
			if (StringUtils.equals(metar.getQualityControlFlags().getLightningSensorOff(), TRUE)) {
				metarStage.setLightningSensorOff(Y);
			}
			if (StringUtils.equals(metar.getQualityControlFlags().getFreezingRainSensorOff(), TRUE)) {
				metarStage.setFreezingRainSensorOff(Y);
			}
			if (StringUtils.equals(metar.getQualityControlFlags().getPresentWeatherSensorOff(), TRUE)) {
				metarStage.setPresentWeatherSensorOff(Y);
			}
		}
		
		if (CollectionUtils.size(metar.getSkyCondition()) >= 1) {
			metarStage.setSkyCover1(metar.getSkyCondition().get(0).getSkyCover());
			metarStage.setCloudBaseFtAgl1(metar.getSkyCondition().get(0).getCloudBaseFtAgl());
		}
		if (CollectionUtils.size(metar.getSkyCondition()) >= 2) {
			metarStage.setSkyCover2(metar.getSkyCondition().get(1).getSkyCover());
			metarStage.setCloudBaseFtAgl2(metar.getSkyCondition().get(1).getCloudBaseFtAgl());
		}
		if (CollectionUtils.size(metar.getSkyCondition()) >= 3) {
			metarStage.setSkyCover3(metar.getSkyCondition().get(2).getSkyCover());
			metarStage.setCloudBaseFtAgl3(metar.getSkyCondition().get(2).getCloudBaseFtAgl());
		}
		if (CollectionUtils.size(metar.getSkyCondition()) >= 4) {
			metarStage.setSkyCover4(metar.getSkyCondition().get(3).getSkyCover());
			metarStage.setCloudBaseFtAgl4(metar.getSkyCondition().get(3).getCloudBaseFtAgl());
		}
		
		return metarStage;
	}

}
