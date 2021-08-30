package io.cornerstone.core.message;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.cornerstone.core.Application;
import io.cornerstone.core.domain.Scope;

public abstract class TopicTestBase extends MessageTestBase {

	@Autowired
	protected Application application;

	@Autowired
	protected Topic<String> testTopic;

	@Test
	void publish() throws Exception {
		for (Scope s : Scope.values()) {
			String message = s.name();
			testTopic.publish(message, s);
			Thread.sleep(100);
			verify(messageProcessor).process(eq(message));
		}
	}

}
