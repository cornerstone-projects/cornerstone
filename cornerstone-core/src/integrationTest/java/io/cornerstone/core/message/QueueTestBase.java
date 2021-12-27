package io.cornerstone.core.message;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;

public abstract class QueueTestBase extends MessageTestBase {

	@Autowired
	protected Queue<String> testQueue;

	@Test
	void consume() throws Exception {
		this.testQueue.produce("test");
		Thread.sleep(100);
		then(this.messageProcessor).should().process(eq("test"));
	}

}
