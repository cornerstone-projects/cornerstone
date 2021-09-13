package io.cornerstone.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.MessageSource;

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
