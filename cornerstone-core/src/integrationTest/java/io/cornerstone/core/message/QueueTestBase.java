package io.cornerstone.core.message;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class QueueTestBase extends MessageTestBase {

	@Autowired
	protected Queue<String> testQueue;

	@Test
	void consume() throws Exception {
		testQueue.produce("test");
		Thread.sleep(100);
		verify(messageProcessor).process(eq("test"));
	}

}
