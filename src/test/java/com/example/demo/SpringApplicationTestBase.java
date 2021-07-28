package com.example.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;

import com.example.demo.core.DefaultApplication;

@SpringApplicationTest(webEnvironment = WebEnvironment.NONE)
@ContextConfiguration(classes = SpringApplicationTestBase.Config.class)
public abstract class SpringApplicationTestBase {

	public static final String USER_USERNAME = "user";
	public static final String ADMIN_USERNAME = "admin";
	public static final String DEFAULT_PASSWORD = "password";
	public static final String ADMIN_ROLE = "ADMIN";

	@SpringBootApplication
	@Primary
	static class Config extends DefaultApplication {

	}
}
