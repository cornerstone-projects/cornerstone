package io.cornerstone.core.event;

import io.cornerstone.core.domain.Scope;
import io.cornerstone.test.containers.UseRedisContainer;
import io.cornerstone.test.mock.ResultCaptor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@UseRedisContainer
@ContextConfiguration
class RedisApplicationEventTopicTests extends ApplicationEventTopicTestBase {

	@SpyBean
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
		RedisApplicationEventTopic redisApplicationEventTopic() {
			return new RedisApplicationEventTopic();
		}

	}

}
