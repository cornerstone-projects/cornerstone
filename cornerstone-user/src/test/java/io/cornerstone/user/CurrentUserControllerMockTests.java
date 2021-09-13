package io.cornerstone.user;

import static io.cornerstone.user.CurrentUserController.PATH_PASSWORD;
import static io.cornerstone.user.CurrentUserController.PATH_PROFILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.client.RestTemplate;

import io.cornerstone.test.ControllerMockTestBase;

class CurrentUserControllerMockTests extends ControllerMockTestBase {

	@Test
	void testGet() throws Exception {
		RestTemplate restTemplate = userRestTemplate();
		User u = restTemplate.getForObject(PATH_PROFILE, User.class);
		assertThat(u).isNotNull();
		assertThat(u.getUsername()).isEqualTo(USER_USERNAME);
		assertThat(u.getPassword()).isNull();
		assertThat(u.getName()).isNotNull();
	}

	@Test
	void update() throws Exception {
		RestTemplate restTemplate = userRestTemplate();
		User user = new User();
		user.setUsername("other"); // not editable
		user.setName("new name");
		user.setPhone("13111111111");
		user.setPassword("iampassword"); // not editable
		User u = restTemplate.patchForObject(PATH_PROFILE, user, User.class);
		assertThat(u).isNotNull();
		assertThat(u.getName()).isEqualTo(user.getName());
		assertThat(u.getPhone()).isEqualTo(user.getPhone());
		assertThat(u.getUsername()).isEqualTo(USER_USERNAME); // username not editable
		User u2 = restTemplate.getForObject(PATH_PROFILE, User.class);
		assertThat(u2).isNotNull();
		assertThat(u2.getName()).isEqualTo(u.getName());
		assertThat(u2.getPhone()).isEqualTo(u.getPhone());

		// assert password not changed
		assertThat(restTemplate.getForEntity(PATH_PROFILE, User.class).getStatusCode()).isSameAs(OK);

		user.setPhone("123456");
		assertThatThrownBy(
				() -> restTemplate.exchange(RequestEntity.method(PATCH, PATH_PROFILE).body(user), User.class))
						.isInstanceOf(BadRequest.class);
	}

	@Test
	void changePassword() throws Exception {
		RestTemplate restTemplate = userRestTemplate();
		ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
		changePasswordRequest.setPassword("iamtest");
		changePasswordRequest.setConfirmedPassword("iamtest2");

		assertThatThrownBy(() -> restTemplate
				.exchange(RequestEntity.method(PUT, PATH_PASSWORD).body(changePasswordRequest), void.class))
						.isInstanceOf(BadRequest.class);
		// caused by wrong confirmed password

		changePasswordRequest.setConfirmedPassword(changePasswordRequest.getPassword());
		assertThatThrownBy(() -> restTemplate
				.exchange(RequestEntity.method(PUT, PATH_PASSWORD).body(changePasswordRequest), void.class))
						.isInstanceOf(BadRequest.class);
		// caused by missing current password

		changePasswordRequest.setCurrentPassword("******");
		assertThatThrownBy(() -> restTemplate
				.exchange(RequestEntity.method(PUT, PATH_PASSWORD).body(changePasswordRequest), void.class))
						.isInstanceOf(BadRequest.class);
		// caused by wrong current password

		changePasswordRequest.setCurrentPassword(DEFAULT_PASSWORD);
		restTemplate.exchange(RequestEntity.method(PUT, PATH_PASSWORD).body(changePasswordRequest), void.class);

		RestTemplate newPasswordRestTemplate = getRestTemplate(USER_USERNAME, changePasswordRequest.getPassword());
		User u = newPasswordRestTemplate.getForObject(PATH_PROFILE, User.class);
		assertThat(u).isNotNull();
		assertThat(u.getUsername()).isEqualTo(USER_USERNAME); // verify password changed

		changePasswordRequest.setCurrentPassword(changePasswordRequest.getPassword());
		changePasswordRequest.setPassword(DEFAULT_PASSWORD);
		changePasswordRequest.setConfirmedPassword(changePasswordRequest.getPassword());
		newPasswordRestTemplate.put(PATH_PASSWORD, changePasswordRequest); // change password back
	}

}
