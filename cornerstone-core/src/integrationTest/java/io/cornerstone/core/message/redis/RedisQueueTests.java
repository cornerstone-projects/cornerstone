package io.cornerstone.core.message.redis;

import io.cornerstone.core.message.MessageProcessor;
import io.cornerstone.core.message.QueueTestBase;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ContextConfiguration(classes = { RedisQueueTests.Config.class, Redis.class })
class RedisQueueTests extends QueueTestBase {

	@SpyBean
	private RedisConnectionFactory connectionFactory;

	@Test
	void produce() {
		ResultCaptor<RedisConnection> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(this.connectionFactory.getConnection()).willAnswer(resultCaptor);
		this.testQueue.produce("test");
		verify(resultCaptor.getResult()).rPush(any(), any());
	}

	static class Config {

		@Bean
		TestQueue testQueue(MessageProcessor messageProcessor) {
			TestQueue queue = new TestQueue(messageProcessor);
			queue.setConsuming(true);
			return queue;
		}

	}

	@RequiredArgsConstructor
	static class TestQueue extends RedisQueue<String> {

		private final MessageProcessor messageProcessor;

		@Override
		public void consume(String message) {
			this.messageProcessor.process(message);
		}

	}

}
