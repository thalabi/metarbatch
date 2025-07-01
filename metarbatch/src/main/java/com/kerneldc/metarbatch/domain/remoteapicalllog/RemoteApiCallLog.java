package com.kerneldc.metarbatch.domain.remoteapicalllog;

import java.time.OffsetDateTime;

import com.kerneldc.metarbatch.domain.AbstractPersistableEntity;
import com.kerneldc.metarbatch.domain.LogicalKeyHolder;
import com.kerneldc.metarbatch.domain.remoteapicall.RemoteApiCall;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class RemoteApiCallLog extends AbstractPersistableEntity {

	private static final long serialVersionUID = 1L;
	
	public enum RetryStatusEnum {NEVER_ATTEMPTED, SUCCESS, RETRY, RETRY_SUCCESS, GIVE_UP} 

    @ManyToOne
    @JoinColumn(name = "remote_api_call_id")
	@Setter(AccessLevel.NONE)
	private RemoteApiCall remoteApiCall;
	
	private Integer attempt;
	
	@Enumerated(EnumType.STRING)
    private RetryStatusEnum status;
	
    private String message;
    private String stackTrace;
    
    @Setter(AccessLevel.NONE)
    private OffsetDateTime timestamp;
    
    private OffsetDateTime nextRetryTime;

	public void setRemoteApiCall(RemoteApiCall remoteApiCall) {
		this.remoteApiCall = remoteApiCall;
		setLogicalKeyHolder();
	}

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
		setLogicalKeyHolder();
	}

	@Override
	protected void setLogicalKeyHolder() {
		var logicalKeyHolder = LogicalKeyHolder.build(remoteApiCall.getRequest(), timestamp);
		super.setLogicalKeyHolder(logicalKeyHolder);
	}

}
