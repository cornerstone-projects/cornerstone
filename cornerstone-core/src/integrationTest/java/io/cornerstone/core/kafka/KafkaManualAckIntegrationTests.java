package io.cornerstone.core.kafka;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.cornerstone.test.containers.UseKafkaContainer;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@UseKafkaContainer
@TestPropertySource(properties = {
		"spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
		"spring.kafka.producer.properties.spring.json.add.type.headers=false",
		"spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer",
		"spring.kafka.properties.spring.deserializer.value.delegate.class=io.cornerstone.core.kafka.PersonDeserializer",
		"spring.kafka.consumer.auto-offset-reset=earliest", "spring.kafka.consumer.enable-auto-commit=false",
		"spring.kafka.listener.ack-mode=MANUAL_IMMEDIATE" })
@SpringJUnitConfig
class KafkaManualAckIntegrationTests {

	private static final String TEST_TOPIC_NAME = "test-topic";

	private static final String TEST_GROUP_NAME = "test-group";

	@Autowired
	private KafkaTemplate<String, Person> kafkaTemplate;

	@MockitoSpyBean
	private TestListener testListener;

	@Test
	void sendAndReceive() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(3);
		willAnswer(invocation -> {
			latch.countDown();
			return invocation.callRealMethod();
		}).given(this.testListener).receive(any(), any());

		this.kafkaTemplate.send(TEST_TOPIC_NAME, new Person("test1", 10));
		this.kafkaTemplate.send(TEST_TOPIC_NAME, new Person("test2", 20));

		assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
		then(this.testListener).should(times(2)).receive(eq(new Person("test1", 10)), any());
		then(this.testListener).should().receive(eq(new Person("test2", 20)), any());
	}

	@Configuration
	static class Config {

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

		AtomicBoolean ready = new AtomicBoolean();

		@KafkaListener(id = TEST_GROUP_NAME, topics = TEST_TOPIC_NAME)
		void receive(@Payload Person person, Acknowledgment ack) {
			if (this.ready.compareAndSet(false, true)) {
				throw new RuntimeException("Not Ready");
			}
			// process message
			ack.acknowledge();
		}

	}

}
