package com.example.demo.user;

import static com.example.demo.MainApplication.DEFAULT_PASSWORD;
import static com.example.demo.MainApplication.USER_USERNAME;
import static com.example.demo.user.CurrentUserController.PATH_PASSWORD;
import static com.example.demo.user.CurrentUserController.PATH_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.example.demo.ControllerTestBase;

class CurrentUserControllerTests extends ControllerTestBase {

	@Test
	void get() {
		TestRestTemplate restTemplate = userRestTemplate();
		User u = restTemplate.getForObject(PATH_PROFILE, User.class);
		assertThat(u.getUsername()).isEqualTo(USER_USERNAME);
		assertThat(u.getPassword()).isNull();
		assertThat(u.getName()).isNotNull();
	}

	@Test
	void update() {
		TestRestTemplate restTemplate = userRestTemplate();
		User user = new User();
		user.setUsername("other");// not editable
		user.setName("new name");
		user.setPhone("13111111111");
		user.setPassword("iampassword"); // not editable
		User u = restTemplate.patchForObject(PATH_PROFILE, user, User.class);
		assertThat(u.getName()).isEqualTo(user.getName());
		assertThat(u.getPhone()).isEqualTo(user.getPhone());
		assertThat(u.getUsername()).isEqualTo(USER_USERNAME); // username not editable
		User u2 = restTemplate.getForObject(PATH_PROFILE, User.class);
		assertThat(u2.getName()).isEqualTo(u.getName());
		assertThat(u2.getPhone()).isEqualTo(u.getPhone());

		// assert password not changed
		assertThat(restTemplate.getForEntity(PATH_PROFILE, User.class).getStatusCode()).isSameAs(OK);

		user.setPhone("123456");
		assertThat(restTemplate.exchange(RequestEntity.method(HttpMethod.PATCH, PATH_PROFILE).body(user), User.class)
				.getStatusCode()).isSameAs(BAD_REQUEST);
	}

	@Test
	void changePassword() {
		TestRestTemplate restTemplate = userRestTemplate();
		PasswordChangeRequest pcr = new PasswordChangeRequest();
		pcr.setPassword("iamtest");
		pcr.setConfirmedPassword("iamtest2");

		ResponseEntity<?> response = restTemplate
				.exchange(RequestEntity.method(HttpMethod.PUT, PATH_PASSWORD).body(pcr), void.class);
		assertThat(response.getStatusCode()).isSameAs(BAD_REQUEST); // caused by wrong confirmed password

		pcr.setConfirmedPassword(pcr.getPassword());
		response = restTemplate.exchange(RequestEntity.method(HttpMethod.PUT, PATH_PASSWORD).body(pcr), void.class);
		assertThat(response.getStatusCode()).isSameAs(BAD_REQUEST); // caused by missing current password

		pcr.setCurrentPassword("******");
		response = restTemplate.exchange(RequestEntity.method(HttpMethod.PUT, PATH_PASSWORD).body(pcr), void.class);
		assertThat(response.getStatusCode()).isSameAs(BAD_REQUEST); // caused by wrong current password

		pcr.setCurrentPassword(DEFAULT_PASSWORD);
		response = restTemplate.exchange(RequestEntity.method(HttpMethod.PUT, PATH_PASSWORD).body(pcr), void.class);
		assertThat(response.getStatusCode()).isSameAs(OK);

		restTemplate = restTemplate.withBasicAuth(USER_USERNAME, pcr.getPassword());
		User u = restTemplate.getForObject(PATH_PROFILE, User.class);
		assertThat(u.getUsername()).isEqualTo(USER_USERNAME); // verify password changed

		pcr.setCurrentPassword(pcr.getPassword());
		pcr.setPassword(DEFAULT_PASSWORD);
		pcr.setConfirmedPassword(pcr.getPassword());
		restTemplate.put(PATH_PASSWORD, pcr); // change password back
	}

}
