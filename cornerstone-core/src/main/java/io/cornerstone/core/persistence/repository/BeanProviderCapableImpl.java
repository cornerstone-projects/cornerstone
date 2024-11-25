package io.cornerstone.core.persistence.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.Environment;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class BeanProviderCapableImpl implements BeanProviderCapable {

	private final BeanFactory beanFactory;

	private final ApplicationContext ctx;

	private final EntityManager entityManager;

	@Override
	public <T> T getBean(Class<T> requiredType) {
		if (requiredType.isInstance(this.beanFactory)) {
			return (T) this.beanFactory;
		}
		if (requiredType.isInstance(this.ctx)) {
			return (T) this.ctx;
		}
		if (requiredType.isInstance(this.entityManager)) {
			return (T) this.entityManager;
		}
		if (requiredType == Environment.class) {
			return (T) this.ctx.getEnvironment();
		}
		return this.ctx.getBean(requiredType);
	}

	@Override
	public <T> T getBean(ResolvableType requiredType) {
		return (T) this.ctx.getBeanProvider(requiredType).getObject();
	}

}
