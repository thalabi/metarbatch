package com.kerneldc.metarbatch.domain.remoteapicall;

import java.time.OffsetDateTime;

import com.kerneldc.metarbatch.domain.AbstractPersistableEntity;
import com.kerneldc.metarbatch.domain.LogicalKeyHolder;
import com.kerneldc.metarbatch.service.http.HttpRequestTypeEnum;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class RemoteApiCall extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;
	
	@Enumerated(EnumType.STRING)
	@Setter(AccessLevel.NONE)
	private HttpRequestTypeEnum request;
	
    private String parameters;
    
    @Setter(AccessLevel.NONE)
    private OffsetDateTime timestamp;


	public void setRequest(HttpRequestTypeEnum request) {
		this.request = request;
		setLogicalKeyHolder();
	}

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
		setLogicalKeyHolder();
	}

	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(request, timestamp);
		super.setLogicalKeyHolder(logicalKeyHolder);
	}
}
