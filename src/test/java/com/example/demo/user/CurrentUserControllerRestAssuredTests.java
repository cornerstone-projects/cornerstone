package com.example.demo.user;

import static com.example.demo.MainApplication.DEFAULT_PASSWORD;
import static com.example.demo.MainApplication.USER_USERNAME;
import static com.example.demo.user.CurrentUserController.PATH_PASSWORD;
import static com.example.demo.user.CurrentUserController.PATH_PROFILE;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.demo.ControllerMockTestBase;

class CurrentUserControllerRestAssuredTests extends ControllerMockTestBase {

	@Test
	void get() throws Exception {
		given().webAppContextSetup(webApplicationContext).auth().with(user()).when().get(PATH_PROFILE).then()
				.assertThat().statusCode(HttpStatus.OK.value()).body("username", equalTo(USER_USERNAME))
				.body("name", notNullValue()).body("password", nullValue());
	}

	@Test
	void update() throws Exception {
		User user = new User();
		user.setUsername("other");// not editable
		user.setName("new name");
		user.setPhone("13111111111");
		user.setPassword("iampassword"); // not editable

		given().webAppContextSetup(webApplicationContext).auth().with(user()).contentType(JSON).body(user).when()
				.patch(PATH_PROFILE).then().assertThat().statusCode(HttpStatus.OK.value())
				.body("username", equalTo(USER_USERNAME)).body("name", equalTo(user.getName()))
				.body("phone", equalTo(user.getPhone()));

		given().webAppContextSetup(webApplicationContext).auth().with(user()).when().get(PATH_PROFILE).then()
				.assertThat().statusCode(HttpStatus.OK.value()).body("username", equalTo(USER_USERNAME))
				.body("name", equalTo(user.getName())).body("phone", equalTo(user.getPhone()));

		user.setPhone("123456");
		given().webAppContextSetup(webApplicationContext).auth().with(user()).contentType(JSON).body(user).when()
				.patch(PATH_PROFILE).then().assertThat().statusCode(HttpStatus.BAD_REQUEST.value());
	}

	@Test
	void changePassword() throws Exception {
		ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
		changePasswordRequest.setPassword("iamtest");
		changePasswordRequest.setConfirmedPassword("iamtest2");

		given().webAppContextSetup(webApplicationContext).auth().with(user()).contentType(JSON)
				.body(changePasswordRequest).when().put(PATH_PASSWORD).then().assertThat()
				.statusCode(HttpStatus.BAD_REQUEST.value());
		// caused by wrong confirmed password

		changePasswordRequest.setConfirmedPassword(changePasswordRequest.getPassword());
		given().webAppContextSetup(webApplicationContext).auth().with(user()).contentType(JSON)
				.body(changePasswordRequest).when().put(PATH_PASSWORD).then().assertThat()
				.statusCode(HttpStatus.BAD_REQUEST.value());
		// caused by missing current password

		changePasswordRequest.setCurrentPassword("******");
		given().webAppContextSetup(webApplicationContext).auth().with(user()).contentType(JSON)
				.body(changePasswordRequest).when().put(PATH_PASSWORD).then().assertThat()
				.statusCode(HttpStatus.BAD_REQUEST.value());
		// caused by wrong current password

		changePasswordRequest.setCurrentPassword(DEFAULT_PASSWORD);
		given().webAppContextSetup(webApplicationContext).auth().with(user()).contentType(JSON)
				.body(changePasswordRequest).when().put(PATH_PASSWORD).then().assertThat()
				.statusCode(HttpStatus.OK.value());

		RequestPostProcessor newPassword = httpBasic(USER_USERNAME, changePasswordRequest.getPassword());

		// verify password changed
		given().webAppContextSetup(webApplicationContext).auth().with(newPassword).when().get(PATH_PROFILE).then()
				.assertThat().statusCode(HttpStatus.OK.value()).body("username", equalTo(USER_USERNAME));

		changePasswordRequest.setCurrentPassword(changePasswordRequest.getPassword());
		changePasswordRequest.setPassword(DEFAULT_PASSWORD);
		changePasswordRequest.setConfirmedPassword(changePasswordRequest.getPassword());
		given().webAppContextSetup(webApplicationContext).auth().with(newPassword).contentType(JSON)
				.body(changePasswordRequest).when().put(PATH_PASSWORD).then().assertThat()
				.statusCode(HttpStatus.OK.value()); // change password back
	}

}
