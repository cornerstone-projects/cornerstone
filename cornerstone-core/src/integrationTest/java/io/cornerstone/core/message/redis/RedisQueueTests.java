package io.cornerstone.core.message.redis;

import io.cornerstone.core.message.MessageProcessor;
import io.cornerstone.core.message.QueueTestBase;
import io.cornerstone.test.containers.UseRedisContainer;
import io.cornerstone.test.mock.ResultCaptor;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@UseRedisContainer
@ContextConfiguration
class RedisQueueTests extends QueueTestBase {

	@MockitoSpyBean
	private RedisConnectionFactory connectionFactory;

	@Test
	void produce() {
		ResultCaptor<RedisListCommands> resultCaptor = new ResultCaptor<>(Mockito::spy);
		given(this.connectionFactory.getConnection().listCommands()).willAnswer(resultCaptor);
		this.testQueue.produce("test");
		then(resultCaptor.getResult()).should().rPush(any(), any());
	}

	@Configuration
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
