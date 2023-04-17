package io.cornerstone.core.message.redis;

import io.cornerstone.core.Application;
import io.cornerstone.core.DefaultApplication;
import io.cornerstone.core.domain.Scope;
import io.cornerstone.core.message.MessageProcessor;
import io.cornerstone.core.message.TopicTestBase;
import io.cornerstone.test.containers.Redis;
import io.cornerstone.test.mock.ResultCaptor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ContextConfiguration(classes = { RedisTopicTests.Config.class, Redis.class })
class RedisTopicTests extends TopicTestBase {

	@SpyBean
	private RedisConnectionFactory connectionFactory;

	@Test
	void publishLocalScopeMessage() {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(this.connectionFactory.getConnection()).willAnswer(resultCaptor);
		this.testTopic.publish(this.event, Scope.LOCAL);
		assertThat(resultCaptor.getResult()).isNull();
		then(this.messageProcessor).should().process(this.event);
	}

	@Test
	void publishApplicationScopeMessage() throws Exception {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(this.connectionFactory.getConnection()).willAnswer(resultCaptor);
		this.testTopic.publish(this.event, Scope.APPLICATION);
		then(resultCaptor.getResult()).should()
			.publish(eq((this.event.getClass().getName() + '.' + this.application.getName()).getBytes()), any());
		Thread.sleep(100); // wait network response
		then(this.messageProcessor).should().process(this.event);
	}

	@Test
	void publishGlobalScopeMessage() throws Exception {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(this.connectionFactory.getConnection()).willAnswer(resultCaptor);
		this.testTopic.publish(this.event, Scope.GLOBAL);
		then(resultCaptor.getResult()).should().publish(eq((this.event.getClass().getName() + '.').getBytes()), any());
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
	static class TestTopic extends RedisTopic<String> {

		private final MessageProcessor messageProcessor;

		@Override
		public void subscribe(String message) {
			this.messageProcessor.process(message);
		}

	}

}
