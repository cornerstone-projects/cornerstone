package io.cornerstone.core.event;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.cornerstone.core.domain.Scope;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@SpringJUnitConfig
abstract class ApplicationEventTopicTestBase {

	@Autowired
	protected EventPublisher eventPublisher;

	@SpyBean
	protected TestLisenter testListener;

	private final CountDownLatch latch = new CountDownLatch(1);

	@BeforeEach
	void init() {
		willAnswer(invocation -> {
			this.latch.countDown();
			return invocation.callRealMethod();
		}).given(this.testListener).listen(any());
	}

	@SneakyThrows
	protected void assertEventListened(TestEvent event) {
		assertThat(this.latch.await(10, TimeUnit.SECONDS)).isTrue();
		then(this.testListener).should().listen(event);
	}

	@ParameterizedTest
	@EnumSource(Scope.class)
	void publish(Scope scope) {
		TestEvent event = new TestEvent(scope.name());
		this.eventPublisher.publish(event, scope);
		assertEventListened(event);
	}

	@Configuration
	static class Config {

		@Bean
		EventPublisher eventPublisher() {
			return new EventPublisher();
		}

		@Bean
		TestLisenter testLisenter() {
			return new TestLisenter();
		}

	}

	static class TestEvent extends BaseEvent<String> {

		private static final long serialVersionUID = 1L;

		TestEvent(String source) {
			super(source);
		}

	}

	static class TestLisenter {

		@EventListener
		void listen(TestEvent event) {
		}

	}

}
