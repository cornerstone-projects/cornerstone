package io.cornerstone.core.event;

import io.cornerstone.core.domain.Scope;
import io.cornerstone.test.containers.UseRedisContainer;
import io.cornerstone.test.mock.ResultCaptor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.BDDMockito.*;

@UseRedisContainer
@ContextConfiguration
class RedisApplicationEventTopicTests extends ApplicationEventTopicTestBase {

	@MockitoSpyBean
	private RedisConnectionFactory connectionFactory;

	@Test
	void publishLocalScopeEvent() {
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.LOCAL);
		assertEventListened(event);
	}

	@Test
	void publishApplicationScopeEvent() {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(this.connectionFactory.getConnection()).willAnswer(resultCaptor);
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.APPLICATION);
		then(resultCaptor.getResult()).should().publish(any(), any());
		assertEventListened(event);
	}

	@Test
	void publishGlobalScopeEvent() {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(this.connectionFactory.getConnection()).willAnswer(resultCaptor);
		TestEvent event = new TestEvent("");
		this.eventPublisher.publish(event, Scope.GLOBAL);
		then(resultCaptor.getResult()).should().publish(any(), any());
		assertEventListened(event);
	}

	@Configuration
	static class Config {

		@Bean
		RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
			RedisMessageListenerContainer container = new RedisMessageListenerContainer();
			container.setConnectionFactory(redisConnectionFactory);
			return container;
		}

		@Bean
		RedisApplicationEventTopic redisApplicationEventTopic() {
			return new RedisApplicationEventTopic();
		}

	}

}
