package io.cornerstone.core.kafka;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.cornerstone.test.containers.UseKafkaContainer;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationSupport;
import org.springframework.kafka.retrytopic.RetryTopicSchedulerWrapper;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.times;

@UseKafkaContainer
@ContextConfiguration(classes = KafkaRetryableTopicIntegrationTests.Config.class)
@TestPropertySource(properties = {
		"spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
		"spring.kafka.producer.properties.spring.json.add.type.headers=false",
		"spring.kafka.consumer.value-deserializer=io.cornerstone.core.kafka.PersonDeserializer",
		"spring.kafka.consumer.auto-offset-reset=earliest", "spring.kafka.consumer.enable-auto-commit=false" })
@SpringJUnitConfig
class KafkaRetryableTopicIntegrationTests {

	private static final String TEST_TOPIC_NAME = "test-topic";

	private static final String TEST_GROUP_NAME = "test-group";

	@Autowired
	private KafkaTemplate<String, Person> kafkaTemplate;

	@SpyBean
	private TestListener testListener;

	@Test
	void sendAndReceive() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(2);
		willAnswer(invocation -> {
			latch.countDown();
			return invocation.callRealMethod();
		}).given(this.testListener).handleDlt(any());

		this.kafkaTemplate.send(TEST_TOPIC_NAME, new Person("test1", 10));
		this.kafkaTemplate.send(TEST_TOPIC_NAME, new Person("test1", 20)); // will success
		this.kafkaTemplate.send(TEST_TOPIC_NAME, new Person("test1", 30));

		assertThat(latch.await(60, TimeUnit.SECONDS)).isTrue();
		then(this.testListener).should(times(3)).receive(eq(new Person("test1", 10)));
		then(this.testListener).should().receive(eq(new Person("test1", 20)));
		then(this.testListener).should(times(3)).receive(eq(new Person("test1", 30)));
	}

	@EnableScheduling
	@ImportAutoConfiguration(TaskSchedulingAutoConfiguration.class)
	static class Config extends RetryTopicConfigurationSupport {

		@Override
		protected void configureCustomizers(CustomizersConfigurer customizersConfigurer) {
			// Use the new 2.9 mechanism to avoid re-fetching the same records after pause
			customizersConfigurer.customizeErrorHandler(eh -> eh.setSeekAfterError(false));
		}

		// @Bean
		// uncomment @Bean to not reuse TaskScheduler from TaskSchedulingAutoConfiguration
		RetryTopicSchedulerWrapper retryTopicSchedulerWrapper() {
			ThreadPoolTaskSchedulerBuilder builder = new ThreadPoolTaskSchedulerBuilder()
				.threadNamePrefix("retry-tpoic-scheduler");
			return new RetryTopicSchedulerWrapper(builder.build());
		}

		@Bean
		NewTopic testTopic() {
			return TopicBuilder.name(TEST_TOPIC_NAME).build();
		}

		@Bean
		TestListener testListener() {
			return new TestListener();
		}

	}

	static class TestListener {

		Logger logger = LoggerFactory.getLogger(getClass());

		@RetryableTopic(kafkaTemplate = "kafkaTemplate", backoff = @Backoff(multiplier = 2))
		@KafkaListener(id = TEST_GROUP_NAME, topics = TEST_TOPIC_NAME)
		void receive(@Payload Person person) {
			this.logger.warn("Try process: {}", person);
			if (person.age() != 20) {
				throw new RuntimeException("Failed to process " + person);
			}
		}

		@DltHandler
		void handleDlt(Person person) {
			this.logger.error("handle DLT: {}", person);
		}

	}

}
