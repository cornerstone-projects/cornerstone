package io.cornerstone.core.message.rabbit;

import io.cornerstone.core.Application;
import io.cornerstone.core.DefaultApplication;
import io.cornerstone.core.domain.Scope;
import io.cornerstone.core.message.MessageProcessor;
import io.cornerstone.core.message.TopicTestBase;
import io.cornerstone.test.containers.RabbitMQ;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ContextConfiguration(classes = { RabbitTopicTests.Config.class, RabbitMQ.class })
public class RabbitTopicTests extends TopicTestBase {

	@SpyBean
	private RabbitTemplate rabbitTemplate;

	@Test
	void publishLocalScopeMessage() {
		this.testTopic.publish(this.event, Scope.LOCAL);
		then(this.rabbitTemplate).should(never())
			.convertAndSend(any(String.class), eq(this.event.getClass().getName()), any(Object.class));
		then(this.messageProcessor).should().process(this.event);
	}

	@Test
	void publishApplicationScopeMessage() throws Exception {
		this.testTopic.publish(this.event, Scope.APPLICATION);
		then(this.rabbitTemplate).should()
			.convertAndSend(any(String.class), eq(this.event.getClass().getName() + '.' + this.application.getName()),
					any(Object.class));
		Thread.sleep(100); // wait network response
		then(this.messageProcessor).should().process(this.event);
	}

	@Test
	void publishGlobalScopeMessage() throws Exception {
		this.testTopic.publish(this.event, Scope.GLOBAL);
		then(this.rabbitTemplate).should()
			.convertAndSend(any(String.class), eq(this.event.getClass().getName() + '.'), any(Object.class));
		Thread.sleep(100); // wait network response
		then(this.messageProcessor).should().process(this.event);
	}

	static class Config {

		@Bean
		Application application() {
			return new DefaultApplication();
		}

		@Bean
		TestTopic testTopic(MessageProcessor messageProcessor) {
			return new TestTopic(messageProcessor);
		}

	}

	@RequiredArgsConstructor
	static class TestTopic extends RabbitTopic<String> {

		private final MessageProcessor messageProcessor;

		@RabbitListener(queues = "#{@testTopic.queueName}")
		@Override
		public void subscribe(String message) {
			this.messageProcessor.process(message);
		}

	}

}
