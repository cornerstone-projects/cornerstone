package com.example.demo;

import static com.example.demo.Application.ADMIN_USERNAME;
import static com.example.demo.Application.DEFAULT_PASSWORD;
import static com.example.demo.Application.USER_USERNAME;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringApplicationTest
public abstract class ControllerTestBase {

	@Autowired
	protected TestRestTemplate testRestTemplate;

	protected TestRestTemplate adminRestTemplate() {
		return testRestTemplate.withBasicAuth(ADMIN_USERNAME, DEFAULT_PASSWORD);
	}

	protected TestRestTemplate userRestTemplate() {
		return testRestTemplate.withBasicAuth(USER_USERNAME, DEFAULT_PASSWORD);
	}

}