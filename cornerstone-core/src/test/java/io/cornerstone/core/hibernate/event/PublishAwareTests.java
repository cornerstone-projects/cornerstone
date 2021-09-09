package io.cornerstone.core.hibernate.event;

import static io.cornerstone.core.hibernate.event.EntityOperationType.CREATE;
import static io.cornerstone.core.hibernate.event.EntityOperationType.DELETE;
import static io.cornerstone.core.hibernate.event.EntityOperationType.UPDATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.cornerstone.core.event.EventPublisher;
import io.cornerstone.test.DataJpaTestBase;

@ContextConfiguration(classes = { PublishAwareTests.Config.class })
@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PublishAwareTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Autowired
	TestService testService;

	@SpyBean
	TestListener testListener;

	@Captor
	ArgumentCaptor<EntityOperationEvent<TestEntity>> eventCaptor;

	@AfterEach
	private void cleanup() {
		repository.deleteAll();
	}

	@Test
	public void cud() {
		TestEntity entity = repository.save(new TestEntity());
		verify(testListener).on(eventCaptor.capture());
		EntityOperationEvent<TestEntity> event = eventCaptor.getValue();
		assertThat(event).isNotNull();
		assertThat(event.getType()).isSameAs(CREATE);
		reset(testListener);

		entity.setName("test");
		entity = repository.save(entity);
		verify(testListener).on(eventCaptor.capture());
		event = eventCaptor.getValue();
		assertThat(event).isNotNull();
		assertThat(event.getType()).isSameAs(UPDATE);
		reset(testListener);

		repository.delete(entity);
		verify(testListener).on(eventCaptor.capture());
		event = eventCaptor.getValue();
		assertThat(event).isNotNull();
		assertThat(event.getType()).isSameAs(DELETE);

	}

	@Test
	public void saveAndUpdate() {
		testService.saveAndUpdate();
		verify(testListener).on(eventCaptor.capture());
		EntityOperationEvent<TestEntity> event = eventCaptor.getValue();
		assertThat(event).isNotNull();
		assertThat(event.getType()).isSameAs(CREATE);
	}

	@Test
	public void saveAndUpdateAndDelete() {
		testService.saveAndUpdateAndDelete();
		verify(testListener).on(eventCaptor.capture());
		EntityOperationEvent<TestEntity> event = eventCaptor.getValue();
		assertThat(event).isNotNull();
		assertThat(event.getType()).isSameAs(DELETE);
	}

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

		@Bean
		TestListener testListener() {
			return new TestListener();
		}
	}

	static class TestService {

		@Autowired
		TestEntityRepository repository;

		@Transactional
		public void saveAndUpdate() {
			TestEntity entity = repository.save(new TestEntity());
			entity.setName("test");
			entity = repository.save(entity);
		}

		@Transactional
		public void saveAndUpdateAndDelete() {
			TestEntity entity = repository.save(new TestEntity());
			entity.setName("test");
			entity = repository.save(entity);
			repository.delete(entity);
		}
	}

	static class TestListener {

		@EventListener
		public void on(EntityOperationEvent<TestEntity> event) {

		}
	}

}
