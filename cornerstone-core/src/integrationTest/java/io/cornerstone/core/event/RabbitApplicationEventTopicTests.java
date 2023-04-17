package io.cornerstone.core.event;

import io.cornerstone.core.domain.Scope;
import io.cornerstone.test.containers.RabbitMQ;
import org.junit.jupiter.api.Test;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ContextConfiguration(classes = { RabbitApplicationEventTopic.class, RabbitMQ.class })
@TestPropertySource(properties = "application-event.topic.type=rabbit")
class RabbitApplicationEventTopicTests extends ApplicationEventTopicTestBase {

	@SpyBean
	private RabbitTemplate rabbitTemplate;

	@Test
	void publishLocalScopeEvent() {
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.LOCAL);
		then(this.rabbitTemplate).should(never())
			.convertAndSend(any(String.class), any(String.class), any(TestEvent.class));
		then(this.testListener).should().listen(event);
	}

	@Test
	void publishApplicationScopeEvent() throws Exception {
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.APPLICATION);
		then(this.rabbitTemplate).should().convertAndSend(any(String.class), any(String.class), eq(event));
		Thread.sleep(100); // wait network response
		then(this.testListener).should().listen(event);
	}

	@Test
	void publishGlobalScopeEvent() throws Exception {
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.GLOBAL);
		then(this.rabbitTemplate).should().convertAndSend(any(String.class), any(String.class), eq(event));
		Thread.sleep(100); // wait network response
		then(this.testListener).should().listen(event);
	}

}
