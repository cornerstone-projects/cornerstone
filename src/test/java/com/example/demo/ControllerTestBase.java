package com.example.demo;

import static com.example.demo.MainApplication.ADMIN_USERNAME;
import static com.example.demo.MainApplication.DEFAULT_PASSWORD;
import static com.example.demo.MainApplication.USER_USERNAME;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.MessageSource;

@SpringApplicationTest
public abstract class ControllerTestBase {

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
