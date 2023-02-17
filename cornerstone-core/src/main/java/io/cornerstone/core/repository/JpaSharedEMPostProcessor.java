package io.cornerstone.core.repository;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.stereotype.Component;

/**
 * Can be deleted if https://github.com/spring-projects/spring-data-jpa/issues/2730 fixed
 *
 */
@Component
public class JpaSharedEMPostProcessor implements BeanFactoryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		BeanDefinition bd = beanFactory.getBeanDefinition("jpaSharedEM_entityManagerFactory");
		if (bd instanceof AbstractBeanDefinition abd) {
			abd.setSynthetic(false);
		}
	}

}
