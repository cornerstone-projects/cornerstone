package io.cornerstone.core.message;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@SpringJUnitConfig
abstract class MessageTestBase {

	protected final String event = "test";

	@MockBean
	protected MessageProcessor messageProcessor;

	private final CountDownLatch latch = new CountDownLatch(1);

	@BeforeEach
	void init() {
		willAnswer(invocation -> {
			this.latch.countDown();
			return null;
		}).given(this.messageProcessor).process(any());
	}

	@SneakyThrows
	protected void assertMessageProcessed(String message) {
		assertThat(this.latch.await(10, TimeUnit.SECONDS)).isTrue();
		then(this.messageProcessor).should().process(message);
	}

}
