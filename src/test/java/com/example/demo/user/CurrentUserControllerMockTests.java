package com.example.demo.user;

import static com.example.demo.MainApplication.DEFAULT_PASSWORD;
import static com.example.demo.MainApplication.USER_USERNAME;
import static com.example.demo.user.CurrentUserController.PATH_PASSWORD;
import static com.example.demo.user.CurrentUserController.PATH_PROFILE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.demo.ControllerMockTestBase;

class CurrentUserControllerMockTests extends ControllerMockTestBase {

	@Test
	void testGet() throws Exception {
		mockMvc.perform(get(PATH_PROFILE).with(user())).andExpect(status().isOk())
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
		mockMvc.perform(patch(PATH_PROFILE).with(user()).contentType(APPLICATION_JSON).content(toJson(user)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name").value(user.getName()))
				.andExpect(jsonPath("$.phone").value(user.getPhone()))
				.andExpect(jsonPath("$.username").value(USER_USERNAME));
		mockMvc.perform(get(PATH_PROFILE).with(user())).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(USER_USERNAME))
				.andExpect(jsonPath("$.name").value(user.getName()))
				.andExpect(jsonPath("$.phone").value(user.getPhone()));

		user.setPhone("123456");
		mockMvc.perform(patch(PATH_PROFILE).with(user()).contentType(APPLICATION_JSON).content(toJson(user)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void changePassword() throws Exception {
		ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
		changePasswordRequest.setPassword("iamtest");
		changePasswordRequest.setConfirmedPassword("iamtest2");

		mockMvc.perform(
				put(PATH_PASSWORD).with(user()).contentType(APPLICATION_JSON).content(toJson(changePasswordRequest)))
				.andExpect(status().isBadRequest());
		// caused by wrong confirmed password

		changePasswordRequest.setConfirmedPassword(changePasswordRequest.getPassword());
		mockMvc.perform(
				put(PATH_PASSWORD).with(user()).contentType(APPLICATION_JSON).content(toJson(changePasswordRequest)))
				.andExpect(status().isBadRequest());
		// caused by missing current password

		changePasswordRequest.setCurrentPassword("******");
		mockMvc.perform(
				put(PATH_PASSWORD).with(user()).contentType(APPLICATION_JSON).content(toJson(changePasswordRequest)))
				.andExpect(status().isBadRequest());
		// caused by wrong current password

		changePasswordRequest.setCurrentPassword(DEFAULT_PASSWORD);
		mockMvc.perform(
				put(PATH_PASSWORD).with(user()).contentType(APPLICATION_JSON).content(toJson(changePasswordRequest)))
				.andExpect(status().isOk());

		RequestPostProcessor newPassword = httpBasic(USER_USERNAME, changePasswordRequest.getPassword());

		// verify password changed
		mockMvc.perform(get(PATH_PROFILE).with(newPassword).contentType(APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(USER_USERNAME));

		changePasswordRequest.setCurrentPassword(changePasswordRequest.getPassword());
		changePasswordRequest.setPassword(DEFAULT_PASSWORD);
		changePasswordRequest.setConfirmedPassword(changePasswordRequest.getPassword());
		mockMvc.perform(put(PATH_PASSWORD).with(newPassword).contentType(APPLICATION_JSON)
				.content(toJson(changePasswordRequest))).andExpect(status().isOk()); // change password back
	}

}
