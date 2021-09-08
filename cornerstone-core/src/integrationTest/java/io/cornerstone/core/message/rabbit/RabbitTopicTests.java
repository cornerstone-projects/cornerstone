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
class RabbitTopicTests extends TopicTestBase {

	@Captor
	ArgumentCaptor<String> routingKeyCaptor;

	@SpyBean
	private RabbitTemplate rabbitTemplate;

	@Test
	void publishLocalScopeMessage() {
		testTopic.publish("test", Scope.LOCAL);
		verify(rabbitTemplate, times(0)).convertAndSend(any(String.class), routingKeyCaptor.capture(),
				any(Object.class));
		verify(messageProcessor).process(eq("test"));
	}

	@Test
	void publishApplicationScopeMessage() throws Exception {
		testTopic.publish("test", Scope.APPLICATION);
		verify(rabbitTemplate).convertAndSend(any(String.class), routingKeyCaptor.capture(), any(Object.class));
		assertThat(routingKeyCaptor.getValue()).endsWith('.' + application.getName());
		Thread.sleep(100); // wait network response
		verify(messageProcessor).process(eq("test"));
	}

	@Test
	void publishGlobalScopeMessage() throws Exception {
		testTopic.publish("test", Scope.GLOBAL);
		verify(rabbitTemplate).convertAndSend(any(String.class), routingKeyCaptor.capture(), any(Object.class));
		assertThat(routingKeyCaptor.getValue()).doesNotEndWith('.' + application.getName());
		Thread.sleep(100); // wait network response
		verify(messageProcessor).process(eq("test"));
	}

	static class Config {

		@Bean
		public Application application() {
			return new DefaultApplication();
		}

		@Bean
		public TestTopic testTopic(MessageProcessor messageProcessor) {
			return new TestTopic(messageProcessor);
		}

	}

	@RequiredArgsConstructor
	static class TestTopic extends RabbitTopic<String> {

		private final MessageProcessor messageProcessor;

		@RabbitListener(queues = "#{@testTopic.queueName}")
		@Override
		public void subscribe(String message) {
			messageProcessor.process(message);
		}

	}

}
