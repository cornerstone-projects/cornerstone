package io.cornerstone.core.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.core.domain.Scope;
import io.cornerstone.test.containers.Redis;
import io.cornerstone.test.mock.ResultCaptor;

@ContextConfiguration(classes = { RedisApplicationEventTopicTests.Config.class, Redis.class })
class RedisApplicationEventTopicTests extends ApplicationEventTopicTestBase {

	@SpyBean
	private RedisConnectionFactory connectionFactory;

	@Test
	void publishLocalScopeEvent() {
		TestEvent event = new TestEvent("");
		eventPublisher.publish(event, Scope.LOCAL);
		verify(testListener).listen(eq(event));
	}

	@Test
	void publishApplicationScopeEvent() throws Exception {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(connectionFactory.getConnection()).willAnswer(resultCaptor);
		TestEvent event = new TestEvent("");
		eventPublisher.publish(event, Scope.APPLICATION);
		verify(resultCaptor.getResult()).publish(any(), any());
		Thread.sleep(100); // wait network response
		verify(testListener).listen(eq(event));
	}

	@Test
	void publishGlobalScopeEvent() throws Exception {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(connectionFactory.getConnection()).willAnswer(resultCaptor);
		TestEvent event = new TestEvent("");
		eventPublisher.publish(event, Scope.GLOBAL);
		verify(resultCaptor.getResult()).publish(any(), any());
		Thread.sleep(100);// wait network response
		verify(testListener).listen(eq(event));
	}

	static class Config {

		@Bean
		public RedisApplicationEventTopic redisApplicationEventTopic() {
			return new RedisApplicationEventTopic();
		}

	}

}
