package io.cornerstone.core.kafka;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.cornerstone.test.containers.UseKafkaContainer;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;

@UseKafkaContainer
@ContextConfiguration(classes = KafkaIntegrationTests.Config.class)
@TestPropertySource(properties = "spring.kafka.consumer.auto-offset-reset=earliest")
@SpringJUnitConfig
class KafkaIntegrationTests {

	private static final String TEST_TOPIC_NAME = "test-topic";

	private static final String TEST_GROUP_NAME = "test-group";

	private static final String TEST_MESSAGE = "test";

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@SpyBean
	private TestListener testListener;

	private final CountDownLatch latch = new CountDownLatch(1);

	@BeforeEach
	void init() {
		willAnswer(invocation -> {
			this.latch.countDown();
			return invocation.callRealMethod();
		}).given(this.testListener).receive(any());
	}

	@SneakyThrows
	protected void assertMessageListened(String message) {
		assertThat(this.latch.await(10, TimeUnit.SECONDS)).isTrue();
		then(this.testListener).should().receive(message);
	}

	@Test
	void sendAndReceive() {
		this.kafkaTemplate.send(TEST_TOPIC_NAME, TEST_MESSAGE);
		assertMessageListened(TEST_MESSAGE);
	}

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
		void receive(String message) {
		}

	}

}
