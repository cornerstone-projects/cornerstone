package io.cornerstone.core.event;

import io.cornerstone.core.domain.Scope;
import io.cornerstone.test.SpringApplicationTestBase;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@TestMethodOrder(OrderAnnotation.class)
@ContextConfiguration
@RecordApplicationEvents
class EventPublisherTests extends SpringApplicationTestBase {

	@Autowired
	ApplicationEvents applicationEvents;

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private EventPublisher eventPublisher;

	@MockitoBean
	private ApplicationEventTopic applicationEventTopic;

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
		assertTestEvent(event);
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
		assertTestEvent(event);
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
		assertTestEvent(event);
	}

	private void assertTestEvent(TestEvent event) {
		assertThat(this.applicationEvents.stream(TestEvent.class).findFirst()).hasValue(event);
	}

	static class TestEvent extends BaseEvent<String> {

		private static final long serialVersionUID = 1L;

		TestEvent(String source) {
			super(source);
		}

	}

}
