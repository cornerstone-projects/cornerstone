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
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@TestMethodOrder(OrderAnnotation.class)
@ContextConfiguration(classes = EventPublisherTests.Config.class)
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
		verify(this.applicationEventTopic).publish(any(InstanceStartupEvent.class), eq(Scope.GLOBAL));
	}

	@Test
	@Order(2)
	void publishLocalScopeEvent() {
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.LOCAL);
		verifyNoInteractions(this.applicationEventTopic);
		verify(this.testListener).listen(eq(event));
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
		verify(this.applicationEventTopic).publish(eq(event), eq(Scope.APPLICATION));
		verify(this.testListener).listen(eq(event));
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
		verify(this.applicationEventTopic).publish(eq(event), eq(Scope.GLOBAL));
		verify(this.testListener).listen(eq(event));
	}

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
