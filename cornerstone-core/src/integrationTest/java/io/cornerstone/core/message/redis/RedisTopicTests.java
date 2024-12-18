package io.cornerstone.core.message.redis;

import io.cornerstone.core.Application;
import io.cornerstone.core.DefaultApplication;
import io.cornerstone.core.domain.Scope;
import io.cornerstone.core.message.MessageProcessor;
import io.cornerstone.core.message.TopicTestBase;
import io.cornerstone.test.containers.UseRedisContainer;
import io.cornerstone.test.mock.ResultCaptor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@UseRedisContainer
@ContextConfiguration
class RedisTopicTests extends TopicTestBase {

	@MockitoSpyBean
	private RedisConnectionFactory connectionFactory;

	@Test
	void publishLocalScopeMessage() {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(this.connectionFactory.getConnection()).willAnswer(resultCaptor);
		this.testTopic.publish(this.event, Scope.LOCAL);
		assertThat(resultCaptor.getResult()).isNull();
		assertMessageProcessed(this.event);
	}

	@Test
	void publishApplicationScopeMessage() {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(this.connectionFactory.getConnection()).willAnswer(resultCaptor);
		this.testTopic.publish(this.event, Scope.APPLICATION);
		then(resultCaptor.getResult()).should()
			.publish(eq((this.event.getClass().getName() + '.' + this.application.getName()).getBytes()), any());
		assertMessageProcessed(this.event);
	}

	@Test
	void publishGlobalScopeMessage() {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(this.connectionFactory.getConnection()).willAnswer(resultCaptor);
		this.testTopic.publish(this.event, Scope.GLOBAL);
		then(resultCaptor.getResult()).should().publish(eq((this.event.getClass().getName() + '.').getBytes()), any());
		assertMessageProcessed(this.event);
	}

	@Configuration
	static class Config {

		@Bean
		Application application() {
			return new DefaultApplication();
		}

		@Bean
		RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
			RedisMessageListenerContainer container = new RedisMessageListenerContainer();
			container.setConnectionFactory(redisConnectionFactory);
			return container;
		}

		@Bean
		TestTopic testTopic(MessageProcessor messageProcessor) {
			return new TestTopic(messageProcessor);
		}

	}

	@RequiredArgsConstructor
	static class TestTopic extends RedisTopic<String> {

		private final MessageProcessor messageProcessor;

		@Override
		public void subscribe(String message) {
			this.messageProcessor.process(message);
		}

	}

}
