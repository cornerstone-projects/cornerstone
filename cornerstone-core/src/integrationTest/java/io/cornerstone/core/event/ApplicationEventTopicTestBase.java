package io.cornerstone.core.event;

import io.cornerstone.core.domain.Scope;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

@ContextConfiguration(classes = ApplicationEventTopicTestBase.Config.class)
@ExtendWith(SpringExtension.class)
abstract class ApplicationEventTopicTestBase {

	@Autowired
	protected EventPublisher eventPublisher;

	@SpyBean
	protected TestLisenter testListener;

	@ParameterizedTest
	@EnumSource(Scope.class)
	void publish(Scope scope) throws Exception {
		TestEvent event = new TestEvent(scope.name());
		this.eventPublisher.publish(event, scope);
		Thread.sleep(100);
		then(this.testListener).should().listen(eq(event));
	}

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
