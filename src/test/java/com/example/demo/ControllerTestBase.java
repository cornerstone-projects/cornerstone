package com.example.demo;

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
		return testRestTemplate.withBasicAuth(ADMIN_USERNAME, DEFAULT_PASSWORD);
	}

	protected TestRestTemplate userRestTemplate() {
		return testRestTemplate.withBasicAuth(USER_USERNAME, DEFAULT_PASSWORD);
	}

}
