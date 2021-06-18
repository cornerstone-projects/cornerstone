package com.example.demo.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.example.demo.ControllerTestBase;
import com.example.demo.model.ResultPage;

import lombok.Data;
import lombok.NoArgsConstructor;

class UserControllerTests extends ControllerTestBase {

	@Test
	void saveWithAuthenticationFailure() {
		TestRestTemplate restTemplate = testRestTemplate;
		User u = new User("test");
		ResponseEntity<User> response = restTemplate.withBasicAuth("invalid_user", "*******").postForEntity("/users", u,
				User.class);
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
	}

	@Test
	void saveWithAccessDenied() {
		TestRestTemplate restTemplate = userRestTemplate();
		User u = new User("test");
		ResponseEntity<User> response = restTemplate.postForEntity("/users", u, User.class);
		assertThat(response.getStatusCode()).isSameAs(FORBIDDEN);
	}

	@Test
	void crud() {
		TestRestTemplate restTemplate = adminRestTemplate();
		User u = new User("test");
		u.setEnabled(true);
		ResponseEntity<User> response = restTemplate.postForEntity("/users", u, User.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		User user = response.getBody();
		Long id = user.getId();
		assertThat(id).isNotNull();
		assertThat(user.getUsername()).isEqualTo(u.getUsername());
		assertThat(user.isEnabled()).isTrue();

		// get
		response = restTemplate.getForEntity("/user/{id}", User.class, id);
		assertThat(response.getStatusCode()).isSameAs(OK);
		assertThat(response.getBody()).isEqualTo(user);

		// update
		User u2 = new User("other");
		u2.setEnabled(false);
		restTemplate.put("/user/{id}", u2, id);
		User u3 = restTemplate.getForEntity("/user/{id}", User.class, id).getBody();
		assertThat(u3.isEnabled()).isEqualTo(u2.isEnabled());
		assertThat(u3.getUsername()).isEqualTo(user.getUsername()); // username not updatable

		// delete
		restTemplate.delete("/user/{id}", id);
		assertThat(restTemplate.getForEntity("/user/{id}", User.class, id).getStatusCode()).isSameAs(NOT_FOUND);
	}

	@Test
	void list() {
		TestRestTemplate restTemplate = userRestTemplate();
		ResponseEntity<ResultPage<User>> response = restTemplate.exchange(
				RequestEntity.method(HttpMethod.GET, URI.create("/users")).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		ResultPage<User> page = response.getBody();
		assertThat(page.getResult().size()).isEqualTo(2);
		assertThat(page.getPageNo()).isEqualTo(1);
		assertThat(page.getPageSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(2);
	}

	@Data
	@NoArgsConstructor
	static class User {

		private Long id;

		private String username;

		private String password;

		private boolean enabled;

		private Set<String> roles = new LinkedHashSet<>();

		public User(String username) {
			this.username = username;
		}

	}

}
