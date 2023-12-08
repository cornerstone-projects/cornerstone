package io.cornerstone.core.hibernate.event;

import io.cornerstone.core.event.EventPublisher;
import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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

import static io.cornerstone.core.hibernate.event.EntityOperationType.CREATE;
import static io.cornerstone.core.hibernate.event.EntityOperationType.DELETE;
import static io.cornerstone.core.hibernate.event.EntityOperationType.UPDATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.reset;

@ContextConfiguration(classes = { PublishAwareTests.Config.class })
@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PublishAwareTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Autowired
	TestService testService;

	@SpyBean
	TestListener testListener;

	@AfterEach
	void cleanup() {
		this.repository.deleteAll();
	}

	@Test
	void cud() {
		TestEntity entity = this.repository.save(new TestEntity());
		then(this.testListener).should().on(argThat(event -> {
			assertThat(event).isNotNull();
			assertThat(event.getType()).isSameAs(CREATE);
			return true;
		}));
		reset(this.testListener);

		entity.setName("test");
		entity = this.repository.save(entity);
		then(this.testListener).should().on(argThat(event -> {
			assertThat(event).isNotNull();
			assertThat(event.getType()).isSameAs(UPDATE);
			return true;
		}));
		reset(this.testListener);

		this.repository.delete(entity);
		then(this.testListener).should().on(argThat(event -> {
			assertThat(event).isNotNull();
			assertThat(event.getType()).isSameAs(DELETE);
			return true;
		}));
	}

	@Test
	void saveAndUpdate() {
		this.testService.saveAndUpdate();
		then(this.testListener).should().on(argThat(event -> {
			assertThat(event).isNotNull();
			assertThat(event.getType()).isSameAs(CREATE);
			return true;
		}));
	}

	@Test
	void saveAndUpdateAndDelete() {
		this.testService.saveAndUpdateAndDelete();
		then(this.testListener).should().on(argThat(event -> {
			assertThat(event).isNotNull();
			assertThat(event.getType()).isSameAs(DELETE);
			return true;
		}));
	}

	@EnableAspectJAutoProxy
	public static class Config {

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

	public static class TestService {

		@Autowired
		TestEntityRepository repository;

		@Transactional
		public void saveAndUpdate() {
			TestEntity entity = this.repository.save(new TestEntity());
			entity.setName("test");
			this.repository.save(entity);
		}

		@Transactional
		public void saveAndUpdateAndDelete() {
			TestEntity entity = this.repository.save(new TestEntity());
			entity.setName("test");
			entity = this.repository.save(entity);
			this.repository.delete(entity);
		}

	}

	public static class TestListener {

		@EventListener
		public void on(EntityOperationEvent<TestEntity> event) {

		}

	}

}
