package io.cornerstone.core.persistence.repository;

import org.springframework.core.ResolvableType;

public interface BeanProviderCapable {

	<T> T getBean(Class<T> requiredType);

	<T> T getBean(ResolvableType requiredType);

}
