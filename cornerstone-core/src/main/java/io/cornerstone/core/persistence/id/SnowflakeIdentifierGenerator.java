package io.cornerstone.core.persistence.id;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.EventType;
import org.hibernate.id.IdentifierGenerator;

public class SnowflakeIdentifierGenerator implements IdentifierGenerator {

	private final Snowflake snowflake;

	public SnowflakeIdentifierGenerator(SnowflakeProperties config) {
		this.snowflake = config.build();
	}

	@Override
	public Object generate(SharedSessionContractImplementor session, Object obj) {
		return this.snowflake.nextId();
	}

	@Override
	public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue,
			EventType eventType) {
		if (currentValue != null) {
			return currentValue;
		}
		return generate(session, owner);
	}

	@Override
	public boolean allowAssignedIdentifiers() {
		return true;
	}

}
