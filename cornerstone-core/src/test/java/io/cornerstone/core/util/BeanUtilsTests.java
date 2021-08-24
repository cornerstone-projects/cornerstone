package io.cornerstone.core.util;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.Data;

public class BeanUtilsTests {

	@Data
	public static class User {

		private Integer id;

		@JsonView(View.A.class)
		private String username;

		@JsonView(View.B.class)
		private String password;

		private Boolean enabled;

		@JsonView(View.A.class)
		@JsonProperty(access = READ_ONLY)
		private String fullname;

		interface View {
			interface A {

			}

			interface B extends A {

			}
		}
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

	@Test
	public void copyPropertiesInJsonView() {
		User user1 = new User();
		user1.setId(1);
		user1.setUsername("username1");
		user1.setPassword("password1");
		user1.setEnabled(false);
		user1.setFullname("fullname1");

		User user2 = new User();
		user2.setEnabled(true);
		BeanUtils.copyPropertiesInJsonView(user1, user2, User.View.A.class);
		assertThat(user2.getId()).isNull();
		assertThat(user2.getUsername()).isEqualTo(user1.getUsername());
		assertThat(user2.getPassword()).isNull();
		assertThat(user2.getEnabled()).isEqualTo(Boolean.TRUE);
		assertThat(user2.getFullname()).isNull(); // readonly

		User user3 = new User();
		user3.setEnabled(true);
		BeanUtils.copyPropertiesInJsonView(user1, user3, User.View.B.class);
		assertThat(user3.getId()).isNull();
		assertThat(user3.getUsername()).isEqualTo(user1.getUsername());
		assertThat(user3.getPassword()).isEqualTo(user1.getPassword());
		assertThat(user3.getEnabled()).isEqualTo(Boolean.TRUE);

	}

}
