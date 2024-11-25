package io.cornerstone.core.persistence.repository.beanprovidercapable;

import io.cornerstone.test.DataJpaTestBase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BeanProviderCapableTests extends DataJpaTestBase {

	@Autowired
	ApplicationContext ctx;

	@Autowired
	BeanFactory beanFactory;

	@Autowired
	Environment environment;

	@Autowired
	EntityManager entityManager;

	@Autowired
	TestEntityRepository repository;

	@Test
	void getApplicationContext() {
		assertThat(this.repository.getApplicationContext()).isSameAs(this.ctx);
	}

	@Test
	void getBeanFactory() {
		assertThat(this.repository.getBeanFactory()).isSameAs(this.beanFactory);
	}

	@Test
	void getEnvironment() {
		assertThat(this.repository.getEnvironment()).isSameAs(this.environment);
	}

	@Test
	void getEntityManager() {
		assertThat(this.repository.getEntityManager()).isSameAs(this.entityManager);
	}

	@Test
	void getSelf() {
		assertThat(this.repository.getSelf()).isSameAs(this.repository);
	}

	@Test
	void getSelfByResolvableType() {
		assertThat(this.repository.getSelfByResolvableType()).isSameAs(this.repository);
	}

}
