package io.cornerstone.core.message.rabbit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.core.Application;
import io.cornerstone.core.DefaultApplication;
import io.cornerstone.core.domain.Scope;
import io.cornerstone.core.message.MessageProcessor;
import io.cornerstone.core.message.TopicTestBase;
import io.cornerstone.test.containers.RabbitMQ;
import lombok.RequiredArgsConstructor;

@ContextConfiguration(classes = { RabbitTopicTests.Config.class, RabbitMQ.class })
public class RabbitTopicTests extends TopicTestBase {

	@Captor
	ArgumentCaptor<String> routingKeyCaptor;

	@SpyBean
	private RabbitTemplate rabbitTemplate;

	@Test
	void publishLocalScopeMessage() {
		this.testTopic.publish("test", Scope.LOCAL);
		verify(this.rabbitTemplate, times(0)).convertAndSend(any(String.class), this.routingKeyCaptor.capture(),
				any(Object.class));
		verify(this.messageProcessor).process(eq("test"));
	}

	@Test
	void publishApplicationScopeMessage() throws Exception {
		this.testTopic.publish("test", Scope.APPLICATION);
		verify(this.rabbitTemplate).convertAndSend(any(String.class), this.routingKeyCaptor.capture(),
				any(Object.class));
		assertThat(this.routingKeyCaptor.getValue()).endsWith('.' + this.application.getName());
		Thread.sleep(100); // wait network response
		verify(this.messageProcessor).process(eq("test"));
	}

	@Test
	void publishGlobalScopeMessage() throws Exception {
		this.testTopic.publish("test", Scope.GLOBAL);
		verify(this.rabbitTemplate).convertAndSend(any(String.class), this.routingKeyCaptor.capture(),
				any(Object.class));
		assertThat(this.routingKeyCaptor.getValue()).doesNotEndWith('.' + this.application.getName());
		Thread.sleep(100); // wait network response
		verify(this.messageProcessor).process(eq("test"));
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
