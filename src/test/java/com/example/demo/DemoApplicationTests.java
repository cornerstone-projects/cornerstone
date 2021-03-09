package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import lombok.Data;

@MyApplicationTest
class DemoApplicationTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void test() {
		User u = new User();
		u.setUsername("username");
		User user = restTemplate.withBasicAuth("user", "password").postForEntity("/users", u, User.class).getBody();
		assertThat(user.getId()).isNotNull();
		assertThat(user.getUsername()).isEqualTo(u.getUsername());
	}

	@Data
	static class User {

		private Long id;

		private String username;

		private String firstname;

		private String lastname;

	}

}
