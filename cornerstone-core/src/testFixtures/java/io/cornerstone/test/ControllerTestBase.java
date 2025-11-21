package io.cornerstone.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.context.MessageSource;

@AutoConfigureTestRestTemplate
@SpringApplicationTest
public abstract class ControllerTestBase extends SpringApplicationTestBase {

	@Autowired
	protected MessageSource messageSource;

	@Autowired
	protected TestRestTemplate testRestTemplate;

	protected TestRestTemplate adminRestTemplate() {
		return this.testRestTemplate.withBasicAuth(ADMIN_USERNAME, DEFAULT_PASSWORD);
	}

	protected TestRestTemplate userRestTemplate() {
		return this.testRestTemplate.withBasicAuth(USER_USERNAME, DEFAULT_PASSWORD);
	}

}
