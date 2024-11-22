package io.cornerstone.core.message.rabbit;

import io.cornerstone.core.message.MessageProcessor;
import io.cornerstone.core.message.QueueTestBase;
import io.cornerstone.test.containers.UseRabbitMQContainer;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.then;

@UseRabbitMQContainer
@ContextConfiguration
class RabbitQueueTests extends QueueTestBase {

	@MockitoSpyBean
	private RabbitTemplate rabbitTemplate;

	@Test
	void produce() {
		String message = "test";
		this.testQueue.produce(message);
		then(this.rabbitTemplate).should().convertAndSend(any(String.class), eq(message));
	}

	@Configuration
	static class Config {

		@Bean
		TestQueue testQueue(MessageProcessor messageProcessor) {
			return new TestQueue(messageProcessor);
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
