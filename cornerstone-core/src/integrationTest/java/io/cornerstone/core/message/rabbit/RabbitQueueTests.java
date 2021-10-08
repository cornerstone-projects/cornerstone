package io.cornerstone.core.message.rabbit;

import io.cornerstone.core.message.MessageProcessor;
import io.cornerstone.core.message.QueueTestBase;
import io.cornerstone.test.containers.RabbitMQ;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ContextConfiguration(classes = { RabbitQueueTests.Config.class, RabbitMQ.class })
class RabbitQueueTests extends QueueTestBase {

	@SpyBean
	private RabbitTemplate rabbitTemplate;

	@Test
	void produce() {
		String message = "test";
		this.testQueue.produce(message);
		verify(this.rabbitTemplate).convertAndSend(any(String.class), eq(message));
	}

	static class Config {

		@Bean
		TestQueue testQueue(MessageProcessor messageProcessor) {
			TestQueue queue = new TestQueue(messageProcessor);
			return queue;
		}

	}

	@RequiredArgsConstructor
	static class TestQueue extends RabbitQueue<String> {

		private final MessageProcessor messageProcessor;

		@RabbitListener(queues = "#{@testQueue.queueName}")
		@Override
		public void consume(String message) {
			this.messageProcessor.process(message);
		}

	}

}
