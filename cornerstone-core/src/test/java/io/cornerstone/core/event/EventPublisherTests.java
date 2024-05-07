package io.cornerstone.core.event;

import io.cornerstone.core.domain.Scope;
import io.cornerstone.test.SpringApplicationTestBase;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;

@TestMethodOrder(OrderAnnotation.class)
@ContextConfiguration
class EventPublisherTests extends SpringApplicationTestBase {

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private EventPublisher eventPublisher;

	@MockBean
	private ApplicationEventTopic applicationEventTopic;

	@SpyBean
	private TestLisenter testListener;

	@Test
	@Order(1)
	void publishApplicationContextEventAsGlobal() {
		then(this.applicationEventTopic).should().publish(any(InstanceStartupEvent.class), eq(Scope.GLOBAL));
	}

	@Test
	@Order(2)
	void publishLocalScopeEvent() {
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.LOCAL);
		then(this.applicationEventTopic).shouldHaveNoInteractions();
		then(this.testListener).should().listen(event);
	}

	@Test
	@Order(3)
	void publishApplicationScopeEvent() {
		willAnswer(invocation -> {
			this.ctx.publishEvent(invocation.getArguments()[0]);
			return null;
		}).given(this.applicationEventTopic).publish(any(TestEvent.class), eq(Scope.APPLICATION));
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.APPLICATION);
		then(this.applicationEventTopic).should().publish(eq(event), eq(Scope.APPLICATION));
		then(this.testListener).should().listen(event);
	}

	@Test
	@Order(4)
	void publishGlobalScopeEvent() {
		willAnswer(invocation -> {
			this.ctx.publishEvent(invocation.getArguments()[0]);
			return null;
		}).given(this.applicationEventTopic).publish(any(TestEvent.class), eq(Scope.GLOBAL));
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.GLOBAL);
		then(this.applicationEventTopic).should().publish(eq(event), eq(Scope.GLOBAL));
		then(this.testListener).should().listen(event);
	}

	@Configuration
	static class Config {

		@Bean
		TestLisenter testLisenter() {
			return new TestLisenter();
		}

	}

	static class TestLisenter {

		@EventListener
		void listen(TestEvent event) {
		}

	}

	static class TestEvent extends BaseEvent<String> {

		private static final long serialVersionUID = 1L;

		TestEvent(String source) {
			super(source);
		}

	}

}
