package com.kerneldc.metarbatch.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Embedded
	@JsonIgnore
	private LogicalKeyHolder logicalKeyHolder;
	
	@Version
	@Column(name = "version")
	private Long version;
	// expose version as rowVersion here since Spring JPA rest does not
	public Long getRowVersion() {
		return version;
	}
	public void setRowVersion(Long rowVersion) {
		version = rowVersion;
	}

//	@CsvBindByName
//	@CsvIgnore(profiles = "csvWrite") // ignore column sourceCsvLineNumber when writing out csv file (when profile is set to csvWrite)
//	@Transient
//	private Long sourceCsvLineNumber;
//	@Transient
//	private String[] sourceCsvLine;
	
    protected abstract void setLogicalKeyHolder();
    
}
