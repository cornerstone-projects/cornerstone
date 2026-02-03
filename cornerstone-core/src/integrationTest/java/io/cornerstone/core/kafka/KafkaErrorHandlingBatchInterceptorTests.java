package io.cornerstone.core.kafka;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.cornerstone.test.containers.UseKafkaContainer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@UseKafkaContainer
@TestPropertySource(properties = {
		"spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer",
		"spring.kafka.producer.properties.spring.json.add.type.headers=false",
		"spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer",
		"spring.kafka.properties.spring.deserializer.value.delegate.class=io.cornerstone.core.kafka.PersonDeserializer",
		"spring.kafka.consumer.auto-offset-reset=earliest", "spring.kafka.consumer.enable-auto-commit=false",
		"spring.kafka.listener.ack-mode=MANUAL_IMMEDIATE", "spring.kafka.listener.type=BATCH" })
@SpringJUnitConfig
class KafkaErrorHandlingBatchInterceptorTests {

	private static final String TEST_TOPIC_NAME = "test-topic";

	private static final String TEST_GROUP_NAME = "test-group";

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@MockitoSpyBean
	private TestListener testListener;

	@MockitoSpyBean
	private ErrorHandlingBatchInterceptor<Object, Object> errorHandlingBatchInterceptor;

	@Test
	void sendAndReceive() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		willAnswer(invocation -> {
			latch.countDown();
			return invocation.callRealMethod();
		}).given(this.testListener).receive(any(), any());

		this.kafkaTemplate.send(TEST_TOPIC_NAME, new Person("test1", 10));
		this.kafkaTemplate.send(TEST_TOPIC_NAME, "invalid");
		this.kafkaTemplate.send(TEST_TOPIC_NAME, new Person("test1", 20));

		assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
		then(this.errorHandlingBatchInterceptor).should().handleError(any(), any(DeserializationException.class));
	}

	@Configuration
	@Import(KafkaConfiguration.class)
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

		@KafkaListener(id = TEST_GROUP_NAME, topics = TEST_TOPIC_NAME)
		void receive(List<ConsumerRecord<String, Person>> records, Acknowledgment ack) {
			// process message
			assertThat(records).hasSize(3);
			assertThat(records).element(1).extracting(ConsumerRecord::value).isNull();
			ack.acknowledge();
		}

	}

}
