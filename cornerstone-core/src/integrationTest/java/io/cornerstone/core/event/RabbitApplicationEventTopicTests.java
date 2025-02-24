package io.cornerstone.core.event;

import io.cornerstone.core.domain.Scope;
import io.cornerstone.test.containers.UseRabbitMQContainer;
import org.junit.jupiter.api.Test;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@UseRabbitMQContainer
@ContextConfiguration(classes = RabbitApplicationEventTopic.class)
@TestPropertySource(properties = "application-event.topic.type=rabbit")
class RabbitApplicationEventTopicTests extends ApplicationEventTopicTestBase {

	@MockitoSpyBean
	private RabbitTemplate rabbitTemplate;

	@Test
	void publishLocalScopeEvent() {
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.LOCAL);
		then(this.rabbitTemplate).should(never())
			.convertAndSend(any(String.class), any(String.class), any(TestEvent.class));
		assertEventListened(event);
	}

	@Test
	void publishApplicationScopeEvent() {
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.APPLICATION);
		then(this.rabbitTemplate).should().convertAndSend(any(String.class), any(String.class), eq(event));
		assertEventListened(event);
	}

	@Test
	void publishGlobalScopeEvent() {
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.GLOBAL);
		then(this.rabbitTemplate).should().convertAndSend(any(String.class), any(String.class), eq(event));
		assertEventListened(event);
	}

}
