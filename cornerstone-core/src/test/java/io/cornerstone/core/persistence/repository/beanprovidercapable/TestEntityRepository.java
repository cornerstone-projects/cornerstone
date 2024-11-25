package io.cornerstone.core.persistence.repository.beanprovidercapable;

import io.cornerstone.core.persistence.repository.BeanProviderCapable;
import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.JpaRepository;

interface TestEntityRepository extends JpaRepository<TestEntity, Long>, BeanProviderCapable {

	default ApplicationContext getApplicationContext() {
		return getBean(ApplicationContext.class);
	}

	default BeanFactory getBeanFactory() {
		return getBean(BeanFactory.class);
	}

	default Environment getEnvironment() {
		return getBean(Environment.class);
	}

	default EntityManager getEntityManager() {
		return getBean(EntityManager.class);
	}

	default TestEntityRepository getSelf() {
		return getBean(TestEntityRepository.class);
	}

	default TestEntityRepository getSelfByResolvableType() {
		return getBean(ResolvableType.forClassWithGenerics(JpaRepository.class, TestEntity.class, Long.class));
	}

}
