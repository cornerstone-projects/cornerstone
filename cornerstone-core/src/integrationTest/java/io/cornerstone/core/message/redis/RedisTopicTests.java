package io.cornerstone.core.message.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.core.Application;
import io.cornerstone.core.DefaultApplication;
import io.cornerstone.core.domain.Scope;
import io.cornerstone.core.message.MessageProcessor;
import io.cornerstone.core.message.TopicTestBase;
import io.cornerstone.test.containers.Redis;
import io.cornerstone.test.mock.ResultCaptor;
import lombok.RequiredArgsConstructor;

@ContextConfiguration(classes = { RedisTopicTests.Config.class, Redis.class })
class RedisTopicTests extends TopicTestBase {

	@Captor
	ArgumentCaptor<byte[]> channelCaptor;

	@SpyBean
	private RedisConnectionFactory connectionFactory;

	@Test
	void publishLocalScopeMessage() throws Exception {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(connectionFactory.getConnection()).willAnswer(resultCaptor);
		testTopic.publish("test", Scope.LOCAL);
		assertThat(resultCaptor.getResult()).isNull();
		verify(messageProcessor).process(eq("test"));
	}

	@Test
	void publishApplicationScopeMessage() throws Exception {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(connectionFactory.getConnection()).willAnswer(resultCaptor);
		testTopic.publish("test", Scope.APPLICATION);
		verify(resultCaptor.getResult()).publish(channelCaptor.capture(), any());
		assertThat(new String(channelCaptor.getValue())).endsWith('.' + application.getName());
		Thread.sleep(100); // wait network response
		verify(messageProcessor).process(eq("test"));
	}

	@Test
	void publishGlobalScopeMessage() throws Exception {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(connectionFactory.getConnection()).willAnswer(resultCaptor);
		testTopic.publish("test", Scope.GLOBAL);
		verify(resultCaptor.getResult()).publish(channelCaptor.capture(), any());
		assertThat(new String(channelCaptor.getValue())).doesNotEndWith('.' + application.getName());
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
	static class TestTopic extends RedisTopic<String> {

		private final MessageProcessor messageProcessor;

		@Override
		public void subscribe(String message) {
			messageProcessor.process(message);
		}

	}

}
