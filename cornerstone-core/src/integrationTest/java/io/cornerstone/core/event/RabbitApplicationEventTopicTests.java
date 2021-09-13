package io.cornerstone.core.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import io.cornerstone.core.domain.Scope;
import io.cornerstone.test.containers.RabbitMQ;

@ContextConfiguration(classes = { RabbitApplicationEventTopic.class, RabbitMQ.class })
@TestPropertySource(properties = "application-event.topic.type=rabbit")
class RabbitApplicationEventTopicTests extends ApplicationEventTopicTestBase {

	@SpyBean
	private RabbitTemplate rabbitTemplate;

	@Test
	void publishLocalScopeEvent() {
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.LOCAL);
		verify(this.rabbitTemplate, times(0)).convertAndSend(any(String.class), any(String.class),
				any(TestEvent.class));
		verify(this.testListener).listen(eq(event));
	}

	@Test
	void publishApplicationScopeEvent() throws Exception {
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.APPLICATION);
		verify(this.rabbitTemplate).convertAndSend(any(String.class), any(String.class), eq(event));
		Thread.sleep(100); // wait network response
		verify(this.testListener).listen(eq(event));
	}

	@Test
	void publishGlobalScopeEvent() throws Exception {
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.GLOBAL);
		verify(this.rabbitTemplate).convertAndSend(any(String.class), any(String.class), eq(event));
		Thread.sleep(100); // wait network response
		verify(this.testListener).listen(eq(event));
	}

}
