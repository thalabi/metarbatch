package com.kerneldc.metarbatch.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Embeddable
@Data
public class LogicalKeyHolder implements Serializable, ILogicallyKeyed {

	private static final long serialVersionUID = 1L;
	private static final String KEY_SEPERATOR = "|";

	@Column(name = COLUMN_LK)
	private String logicalKey;
	
	public static LogicalKeyHolder build(Object... keyParts) {
		var lk = new LogicalKeyHolder();
		String[] stringKeyParts = new String[keyParts.length];
		var i = 0;
		for (Object keyPart: keyParts) {
			if (keyPart == null) {
				stringKeyParts[i++] = StringUtils.EMPTY;
				continue;
			}
			switch (keyPart.getClass().getSimpleName()) {
			case "String":
				stringKeyParts[i++] = (String)keyPart;
				break;
			case "Integer", "Double", "Long":
				stringKeyParts[i++] = String.valueOf(keyPart);	
				break;
			case "LocalDateTime":
				stringKeyParts[i++] = ((LocalDateTime)keyPart).format(AbstractEntity.LOCAL_DATE_TIME_FORMATTER);
				break;
			case "OffsetDateTime":
				stringKeyParts[i++] = ((OffsetDateTime)keyPart).format(AbstractEntity.OFFSET_DATE_TIME_FORMATTER);
				break;
			default:
				throw new IllegalArgumentException(String.format("Unsupported data type in logical key: %s", keyPart.getClass().getSimpleName()));
			}
		}
		lk.setLogicalKey(concatLogicalKeyParts(stringKeyParts));
		return lk;
	}

	private static String concatLogicalKeyParts(String... keyParts) {
    	return String.join(KEY_SEPERATOR, keyParts);
    }	
}
