package io.cornerstone.core.event;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.cornerstone.core.domain.Scope;

@ContextConfiguration(classes = ApplicationEventTopicTestBase.Config.class)
@ExtendWith(SpringExtension.class)
abstract class ApplicationEventTopicTestBase {

	@Autowired
	protected EventPublisher eventPublisher;

	@SpyBean
	protected TestLisenter testListener;

	@Test
	void publish() throws Exception {
		for (Scope s : Scope.values()) {
			TestEvent event = new TestEvent(s.name());
			eventPublisher.publish(event, s);
			Thread.sleep(100);
			verify(testListener).listen(eq(event));
		}
	}

	static class Config {

		@Bean
		public EventPublisher eventPublisher() {
			return new EventPublisher();
		}

		@Bean
		TestLisenter testLisenter() {
			return new TestLisenter();
		}
	}

	static class TestEvent extends BaseEvent<String> {

		private static final long serialVersionUID = 1L;

		public TestEvent(String source) {
			super(source);
		}

	}

	static class TestLisenter {

		@EventListener
		public void listen(TestEvent event) {
		}
	}

}
