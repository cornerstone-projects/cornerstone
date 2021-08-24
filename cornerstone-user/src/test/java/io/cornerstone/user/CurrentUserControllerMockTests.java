package io.cornerstone.user;

import static io.cornerstone.user.CurrentUserController.PATH_PASSWORD;
import static io.cornerstone.user.CurrentUserController.PATH_PROFILE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import io.cornerstone.test.ControllerMockTestBase;
import io.cornerstone.test.MockMvcRestTemplate;

class CurrentUserControllerMockTests extends ControllerMockTestBase {

	@Test
	void get() throws Exception {
		userRestTemplate().getForResult(PATH_PROFILE).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(USER_USERNAME)).andExpect(jsonPath("$.name").exists())
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void update() throws Exception {
		User user = new User();
		user.setUsername("other");// not editable
		user.setName("new name");
		user.setPhone("13111111111");
		user.setPassword("iampassword"); // not editable
		userRestTemplate().patchForResult(PATH_PROFILE, user).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value(user.getName()))
				.andExpect(jsonPath("$.phone").value(user.getPhone()))
				.andExpect(jsonPath("$.username").value(USER_USERNAME));
		userRestTemplate().getForResult(PATH_PROFILE).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(USER_USERNAME))
				.andExpect(jsonPath("$.name").value(user.getName()))
				.andExpect(jsonPath("$.phone").value(user.getPhone()));

		user.setPhone("123456");
		userRestTemplate().patchForResult(PATH_PROFILE, user).andExpect(status().isBadRequest());
	}

	@Test
	void changePassword() throws Exception {
		ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
		changePasswordRequest.setPassword("iamtest");
		changePasswordRequest.setConfirmedPassword("iamtest2");

		userRestTemplate().putForResult(PATH_PASSWORD, changePasswordRequest).andExpect(status().isBadRequest());
		// caused by wrong confirmed password

		changePasswordRequest.setConfirmedPassword(changePasswordRequest.getPassword());
		userRestTemplate().putForResult(PATH_PASSWORD, changePasswordRequest).andExpect(status().isBadRequest());
		// caused by missing current password

		changePasswordRequest.setCurrentPassword("******");
		userRestTemplate().putForResult(PATH_PASSWORD, changePasswordRequest).andExpect(status().isBadRequest());
		// caused by wrong current password

		changePasswordRequest.setCurrentPassword(DEFAULT_PASSWORD);
		userRestTemplate().putForResult(PATH_PASSWORD, changePasswordRequest).andExpect(status().isOk());

		RequestPostProcessor newPassword = httpBasic(USER_USERNAME, changePasswordRequest.getPassword());

		// verify password changed
		MockMvcRestTemplate newPasswordRestTemplate = userRestTemplate().with(newPassword);
		newPasswordRestTemplate.getForResult(PATH_PROFILE).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(USER_USERNAME));

		changePasswordRequest.setCurrentPassword(changePasswordRequest.getPassword());
		changePasswordRequest.setPassword(DEFAULT_PASSWORD);
		changePasswordRequest.setConfirmedPassword(changePasswordRequest.getPassword());
		newPasswordRestTemplate.putForResult(PATH_PASSWORD, changePasswordRequest).andExpect(status().isOk());
		// change password back
	}

}
