package io.cornerstone.core.message;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class QueueTestBase extends MessageTestBase {

	@Autowired
	protected Queue<String> testQueue;

	@Test
	void consume() {
		this.testQueue.produce("test");
		assertMessageProcessed("test");
	}

}
