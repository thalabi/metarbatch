package com.kerneldc.metarbatch.domain.stationidsets;


import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.kerneldc.metarbatch.domain.AbstractPersistableEntity;
import com.kerneldc.metarbatch.domain.LogicalKeyHolder;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class StationIdSets extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;
	
	@Setter(AccessLevel.NONE)
	private String username;
	
	@JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> nameToStationIdSetsMap;
	
	public void setUsername(String username) {
		this.username = username;
		setLogicalKeyHolder();
	}

	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(username);
		super.setLogicalKeyHolder(logicalKeyHolder);
	}
}
