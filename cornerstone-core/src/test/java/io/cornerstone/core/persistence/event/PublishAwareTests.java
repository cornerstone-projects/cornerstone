package io.cornerstone.core.persistence.event;

import io.cornerstone.core.event.EventPublisher;
import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static io.cornerstone.core.persistence.event.EntityOperationType.*;
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration
@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@RecordApplicationEvents
class PublishAwareTests extends DataJpaTestBase {

	@Autowired
	ApplicationEvents applicationEvents;

	@Autowired
	TestEntityRepository repository;

	@Autowired
	TestService testService;

	@AfterEach
	void cleanup() {
		this.repository.deleteAll();
	}

	@Test
	void cud() {
		TestEntity entity = this.repository.save(new TestEntity());
		assertEntityOperationEvent(CREATE);
		this.applicationEvents.clear();

		entity.setName("test");
		entity = this.repository.save(entity);
		assertEntityOperationEvent(UPDATE);
		this.applicationEvents.clear();

		this.repository.delete(entity);
		assertEntityOperationEvent(DELETE);
	}

	@Test
	void saveAndUpdate() {
		this.testService.saveAndUpdate();
		assertEntityOperationEvent(CREATE);

	}

	@Test
	void saveAndUpdateAndDelete() {
		this.testService.saveAndUpdateAndDelete();
		assertEntityOperationEvent(DELETE);
	}

	private void assertEntityOperationEvent(EntityOperationType type) {
		assertThat(this.applicationEvents.stream(EntityOperationEvent.class).findFirst().get()).extracting("type")
			.isSameAs(type);
	}

	@Configuration
	@EnableAspectJAutoProxy
	static class Config {

		@Bean
		EventPublisher eventPublisher() {
			return new EventPublisher();
		}

		@Bean
		EventListenerForPublish eventListenerForPublish() {
			return new EventListenerForPublish();
		}

		@Bean
		PublishAspect publishAspect() {
			return new PublishAspect();
		}

		@Bean
		TestService testService() {
			return new TestService();
		}

	}

	static class TestService {

		@Autowired
		TestEntityRepository repository;

		@Transactional
		void saveAndUpdate() {
			TestEntity entity = this.repository.save(new TestEntity());
			entity.setName("test");
			this.repository.save(entity);
		}

		@Transactional
		void saveAndUpdateAndDelete() {
			TestEntity entity = this.repository.save(new TestEntity());
			entity.setName("test");
			entity = this.repository.save(entity);
			this.repository.delete(entity);
		}

	}

}
