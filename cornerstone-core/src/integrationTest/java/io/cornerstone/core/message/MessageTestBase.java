package io.cornerstone.core.message;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
abstract class MessageTestBase {

	@MockBean
	protected MessageProcessor messageProcessor;

}
