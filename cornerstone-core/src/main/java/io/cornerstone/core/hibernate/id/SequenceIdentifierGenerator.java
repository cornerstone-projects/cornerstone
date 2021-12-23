package io.cornerstone.core.hibernate.id;

import java.io.Serializable;
import java.util.Properties;

import io.cornerstone.core.sequence.Sequence;
import lombok.RequiredArgsConstructor;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import org.springframework.beans.factory.BeanFactory;

@RequiredArgsConstructor
public class SequenceIdentifierGenerator implements IdentifierGenerator {

	private final BeanFactory beanFactory;

	private Sequence sequence;

	private Type type;

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object obj) {
		return this.type instanceof IntegerType ? this.sequence.nextIntValue()
				: this.type instanceof StringType ? this.sequence.nextStringValue() : this.sequence.nextLongValue();
	}

	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
		this.type = type;
		String sequenceName = (String) params.get("sequenceName");
		if (sequenceName == null) {
			throw new IllegalArgumentException("@GenericGenerator miss parameter \"sequenceName\"");
		}
		this.sequence = this.beanFactory.getBean(sequenceName, Sequence.class);
	}

}
