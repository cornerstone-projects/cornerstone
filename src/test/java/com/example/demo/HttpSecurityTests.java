package com.example.demo;

import static com.example.demo.user.UserController.PATH_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

class HttpSecurityTests extends ControllerTestBase {

	@Test
	void testAuthenticationFailure() {
		TestRestTemplate restTemplate = testRestTemplate;
		User u = new User("test");
		ResponseEntity<User> response = restTemplate.withBasicAuth("invalid_user", "*******").postForEntity(PATH_LIST,
				u, User.class);
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
	}

	@Test
	void testAccessDenied() {
		TestRestTemplate restTemplate = userRestTemplate();
		User u = new User("test");
		ResponseEntity<User> response = restTemplate.postForEntity(PATH_LIST, u, User.class);
		assertThat(response.getStatusCode()).isSameAs(FORBIDDEN);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class User {

		private String username;

	}

}
