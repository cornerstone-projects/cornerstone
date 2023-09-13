package io.cornerstone.core.message;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
abstract class MessageTestBase {

	protected final String event = "test";

	@MockBean
	protected MessageProcessor messageProcessor;

}
