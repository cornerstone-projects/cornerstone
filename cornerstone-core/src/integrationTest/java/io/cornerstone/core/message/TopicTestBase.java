package io.cornerstone.core.message;

import io.cornerstone.core.Application;
import io.cornerstone.core.domain.Scope;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public abstract class TopicTestBase extends MessageTestBase {

	@Autowired
	protected Application application;

	@Autowired
	protected Topic<String> testTopic;

	@ParameterizedTest
	@EnumSource(Scope.class)
	void publish(Scope scope) throws Exception {
		String message = scope.name();
		this.testTopic.publish(message, scope);
		Thread.sleep(100);
		verify(this.messageProcessor).process(eq(message));
	}

}