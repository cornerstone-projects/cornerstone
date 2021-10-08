package io.cornerstone.test;

import io.cornerstone.core.DefaultApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;

@SpringApplicationTest(webEnvironment = WebEnvironment.NONE)
@ContextConfiguration(classes = SpringApplicationTestBase.TestApplication.class)
public abstract class SpringApplicationTestBase {

	public static final String USER_USERNAME = "user";

	public static final String ADMIN_USERNAME = "admin";

	public static final String DEFAULT_PASSWORD = "password";

	public static final String ADMIN_ROLE = "ADMIN";

	@SpringBootApplication(scanBasePackageClasses = DefaultApplication.class)
	@Primary
	static class TestApplication extends DefaultApplication {

	}

}
