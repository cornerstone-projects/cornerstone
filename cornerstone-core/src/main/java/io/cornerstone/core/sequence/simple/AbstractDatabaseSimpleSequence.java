package io.cornerstone.core.sequence.simple;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractDatabaseSimpleSequence extends AbstractSimpleSequence {

	private DataSource dataSource;

	private String tableName = DEFAULT_TABLE_NAME;

	private int cacheSize = 1;

	protected String getActualSequenceName() {
		return getSequenceName() + "_SEQ";
	}

}
