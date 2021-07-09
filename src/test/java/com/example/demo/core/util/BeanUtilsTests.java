package com.example.demo.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import lombok.Data;

public class BeanUtilsTests {

	@Data
	public static class User {

		private Integer id;

		private String username;

		private String password;

		private Boolean enabled;
	}

	@Test
	public void copyNonNullProperties() {
		User user1 = new User();
		user1.setId(1);
		user1.setUsername("username");
		user1.setEnabled(false);

		User user2 = new User();
		user2.setPassword("password");

		BeanUtils.copyNonNullProperties(user1, user2);
		assertThat(user2.getId()).isEqualTo(user1.getId());
		assertThat(user2.getUsername()).isEqualTo(user1.getUsername());
		assertThat(user2.getPassword()).isNotNull();
		assertThat(user2.getEnabled()).isEqualTo(user1.getEnabled());

		User user3 = new User();
		user3.setPassword("password");
		BeanUtils.copyNonNullProperties(user1, user3, "id", "enabled");
		assertThat(user3.getId()).isNull();
		assertThat(user3.getEnabled()).isNull();
		assertThat(user2.getUsername()).isEqualTo(user1.getUsername());
	}

}
