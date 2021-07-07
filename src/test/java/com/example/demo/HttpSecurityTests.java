package com.example.demo;

import static com.example.demo.user.UserController.PATH_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.example.demo.user.User;

class HttpSecurityTests extends ControllerTestBase {

	@Test
	void testAuthenticationFailure() {
		TestRestTemplate restTemplate = testRestTemplate;
		User u = new User();
		ResponseEntity<User> response = restTemplate.withBasicAuth("invalid_user", "*******").postForEntity(PATH_LIST,
				u, User.class);
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
	}

	@Test
	void testAccessDenied() {
		TestRestTemplate restTemplate = userRestTemplate();
		User u = new User();
		ResponseEntity<User> response = restTemplate.postForEntity(PATH_LIST, u, User.class);
		assertThat(response.getStatusCode()).isSameAs(FORBIDDEN);
	}

}
