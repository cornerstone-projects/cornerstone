package com.example.demo.user;

import static com.example.demo.MainApplication.DEFAULT_PASSWORD;
import static com.example.demo.MainApplication.USER_USERNAME;
import static com.example.demo.user.CurrentUserController.PATH_PASSWORD;
import static com.example.demo.user.CurrentUserController.PATH_PROFILE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
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
		mockMvc.perform(patch(PATH_PROFILE).with(user()).contentType(MediaType.APPLICATION_JSON).content(toJson(user)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.name").value(user.getName()))
				.andExpect(jsonPath("$.phone").value(user.getPhone()))
				.andExpect(jsonPath("$.username").value(USER_USERNAME));
		mockMvc.perform(get(PATH_PROFILE).with(user())).andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value(USER_USERNAME))
				.andExpect(jsonPath("$.name").value(user.getName()))
				.andExpect(jsonPath("$.phone").value(user.getPhone()));

		user.setPhone("123456");
		mockMvc.perform(patch(PATH_PROFILE).with(user()).contentType(MediaType.APPLICATION_JSON).content(toJson(user)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void changePassword() throws Exception {
		PasswordChangeRequest pcr = new PasswordChangeRequest();
		pcr.setPassword("iamtest");
		pcr.setConfirmedPassword("iamtest2");

		mockMvc.perform(put(PATH_PASSWORD).with(user()).contentType(MediaType.APPLICATION_JSON).content(toJson(pcr)))
				.andExpect(status().isBadRequest());
		// caused by wrong confirmed password

		pcr.setConfirmedPassword(pcr.getPassword());
		mockMvc.perform(put(PATH_PASSWORD).with(user()).contentType(MediaType.APPLICATION_JSON).content(toJson(pcr)))
				.andExpect(status().isBadRequest());
		// caused by missing current password

		pcr.setCurrentPassword("******");
		mockMvc.perform(put(PATH_PASSWORD).with(user()).contentType(MediaType.APPLICATION_JSON).content(toJson(pcr)))
				.andExpect(status().isBadRequest());
		// caused by wrong current password

		pcr.setCurrentPassword(DEFAULT_PASSWORD);
		mockMvc.perform(put(PATH_PASSWORD).with(user()).contentType(MediaType.APPLICATION_JSON).content(toJson(pcr)))
				.andExpect(status().isOk());

		RequestPostProcessor newPassword = httpBasic(USER_USERNAME, pcr.getPassword());

		// verify password changed
		mockMvc.perform(get(PATH_PROFILE).with(newPassword).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$.username").value(USER_USERNAME));

		pcr.setCurrentPassword(pcr.getPassword());
		pcr.setPassword(DEFAULT_PASSWORD);
		pcr.setConfirmedPassword(pcr.getPassword());
		mockMvc.perform(
				put(PATH_PASSWORD).with(newPassword).contentType(MediaType.APPLICATION_JSON).content(toJson(pcr)))
				.andExpect(status().isOk()); // change password back
	}

}
