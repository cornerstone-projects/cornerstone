package io.cornerstone.core.sequence.cyclic;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractDatabaseCyclicSequence extends AbstractCyclicSequence {

	private DataSource dataSource;

	private String tableName = DEFAULT_TABLE_NAME;

	private int cacheSize = 1;

	protected String getActualSequenceName() {
		return getSequenceName() + "_SEQ";
	}

}
