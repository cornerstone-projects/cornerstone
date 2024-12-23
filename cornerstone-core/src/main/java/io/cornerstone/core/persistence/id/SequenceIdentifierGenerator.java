package io.cornerstone.core.persistence.id;

import java.io.Serializable;
import java.lang.reflect.Member;
import java.util.Properties;

import io.cornerstone.core.sequence.Sequence;
import lombok.RequiredArgsConstructor;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.AnnotationBasedGenerator;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import org.springframework.beans.factory.BeanFactory;

@RequiredArgsConstructor
public class SequenceIdentifierGenerator implements IdentifierGenerator, AnnotationBasedGenerator<SequenceIdentifier> {

	private final BeanFactory beanFactory;

	private Sequence sequence;

	private Class<?> type;

	private String sequenceName;

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object obj) {
		return (this.type == Integer.class || this.type == int.class) ? this.sequence.nextIntValue()
				: this.type == String.class ? this.sequence.nextStringValue() : this.sequence.nextLongValue();
	}

	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
		this.type = type.getReturnedClass();
		if (this.sequenceName == null) {
			this.sequenceName = (String) params.get("sequenceName");
			if (this.sequenceName == null) {
				throw new IllegalArgumentException("@GenericGenerator miss parameter \"sequenceName\"");
			}
		}
		this.sequence = this.beanFactory.getBean(this.sequenceName, Sequence.class);
	}

	@Override
	public void initialize(SequenceIdentifier annotation, Member member,
			GeneratorCreationContext generatorCreationContext) {
		this.sequenceName = annotation.value();
	}

}
