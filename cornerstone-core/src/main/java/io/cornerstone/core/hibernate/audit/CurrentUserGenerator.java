package io.cornerstone.core.hibernate.audit;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.EnumSet;

import lombok.Getter;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.generator.GeneratorCreationContext;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class CurrentUserGenerator implements BeforeExecutionGenerator {

	private final Class<?> propertyType;

	@Getter
	private final EnumSet<EventType> eventTypes;

	public CurrentUserGenerator(CreationUser annotation, Member member, GeneratorCreationContext context) {
		this.propertyType = getPropertyType(member);
		this.eventTypes = EventTypeSets.INSERT_ONLY;
	}

	public CurrentUserGenerator(UpdateUser annotation, Member member, GeneratorCreationContext context) {
		this.propertyType = getPropertyType(member);
		this.eventTypes = EventTypeSets.INSERT_AND_UPDATE;
	}

	@Override
	public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue,
			EventType eventType) {
		if (UserDetails.class.isAssignableFrom(this.propertyType)) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			return (auth != null) && this.propertyType.isInstance(auth.getPrincipal()) ? auth.getPrincipal() : null;
		}
		else if (String.class == this.propertyType) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			return auth != null ? auth.getName() : null;
		}
		else {
			throw new HibernateException(
					"Unsupported property type for generator annotation @CreationUser: " + this.propertyType.getName());
		}
	}

	static Class<?> getPropertyType(Member member) {
		if (member instanceof Field) {
			return ((Field) member).getType();
		}
		else if (member instanceof Method) {
			return ((Method) member).getReturnType();
		}
		else {
			throw new AssertionFailure("member should have been a method or field");
		}
	}

}
