package com.kerneldc.metarbatch.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter
public abstract class AbstractPersistableEntity extends AbstractEntity implements Serializable {
	
	protected AbstractPersistableEntity() {
		this.logicalKeyHolder = new LogicalKeyHolder();
	}

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default_seq_gen")
	private Long id;
	
	@Embedded
	@JsonIgnore
	private LogicalKeyHolder logicalKeyHolder;
	
	@Version
	@Column(name = "version")
	private Long version;

	@Transient
	private Long sourceCsvLineNumber;
	@Transient
	private String[] sourceCsvLine;
	
    protected abstract void setLogicalKeyHolder();
}
