package io.cornerstone.core.message.rabbit;

import io.cornerstone.core.Application;
import io.cornerstone.core.DefaultApplication;
import io.cornerstone.core.domain.Scope;
import io.cornerstone.core.message.MessageProcessor;
import io.cornerstone.core.message.TopicTestBase;
import io.cornerstone.test.containers.UseRabbitMQContainer;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@UseRabbitMQContainer
@ContextConfiguration
class RabbitTopicTests extends TopicTestBase {

	@SpyBean
	private RabbitTemplate rabbitTemplate;

	@Test
	void publishLocalScopeMessage() {
		this.testTopic.publish(this.event, Scope.LOCAL);
		then(this.rabbitTemplate).should(never())
			.convertAndSend(any(String.class), eq(this.event.getClass().getName()), any(Object.class));
		assertMessageProcessed(this.event);
	}

	@Test
	void publishApplicationScopeMessage() {
		this.testTopic.publish(this.event, Scope.APPLICATION);
		then(this.rabbitTemplate).should()
			.convertAndSend(any(String.class), eq(this.event.getClass().getName() + '.' + this.application.getName()),
					any(Object.class));
		assertMessageProcessed(this.event);
	}

	@Test
	void publishGlobalScopeMessage() {
		this.testTopic.publish(this.event, Scope.GLOBAL);
		then(this.rabbitTemplate).should()
			.convertAndSend(any(String.class), eq(this.event.getClass().getName() + '.'), any(Object.class));
		assertMessageProcessed(this.event);
	}

	@Configuration
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
