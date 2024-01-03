package io.cornerstone.core.persistence.id;

import java.io.Serializable;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class SnowflakeIdentifierGenerator implements IdentifierGenerator {

	private final Snowflake snowflake;

	public SnowflakeIdentifierGenerator(SnowflakeProperties config) {
		this.snowflake = config.build();
	}

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object obj) {
		return this.snowflake.nextId();
	}

}
