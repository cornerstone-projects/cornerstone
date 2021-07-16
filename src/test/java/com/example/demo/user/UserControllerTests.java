package com.example.demo.user;

import static com.example.demo.MainApplication.ADMIN_USERNAME;
import static com.example.demo.MainApplication.DEFAULT_PASSWORD;
import static com.example.demo.user.UserController.PATH_DETAIL;
import static com.example.demo.user.UserController.PATH_LIST;
import static com.example.demo.user.UserController.PATH_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.example.demo.ControllerTestBase;
import com.example.demo.core.hibernate.domain.ResultPage;

class UserControllerTests extends ControllerTestBase {

	@Test
	void crud() {
		TestRestTemplate restTemplate = adminRestTemplate();
		User u = new User();
		u.setName("test");
		u.setDisabled(true);

		// create
		u.setUsername(ADMIN_USERNAME);
		ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
				RequestEntity.method(POST, URI.create(PATH_LIST)).body(u),
				new ParameterizedTypeReference<Map<String, Object>>() {
				});
		assertThat(resp.getStatusCode()).isSameAs(BAD_REQUEST);
		assertThat(resp.getBody().get("message"))
				.isEqualTo(messageSource.getMessage("username.already.exists", null, LocaleContextHolder.getLocale()));
		u.setUsername("test");
		ResponseEntity<User> response = restTemplate.postForEntity(PATH_LIST, u, User.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		User user = response.getBody();
		assertThat(user).isNotNull();
		assertThat(user.getId()).isNotNull();
		assertThat(user.getUsername()).isEqualTo(u.getUsername());
		assertThat(user.getDisabled()).isEqualTo(u.getDisabled());
		Long id = user.getId();

		// read
		response = restTemplate.getForEntity(PATH_DETAIL, User.class, id);
		assertThat(response.getStatusCode()).isSameAs(OK);
		assertThat(response.getBody()).isEqualTo(user);

		// update partial
		User u2 = new User();
		u2.setUsername("other");
		u2.setName("new name");
		u2.setDisabled(true);
		User u3 = restTemplate.patchForObject(PATH_DETAIL, u2, User.class, id);
		assertThat(u3.getDisabled()).isEqualTo(u2.getDisabled());
		assertThat(u3.getUsername()).isEqualTo(user.getUsername()); // username not updatable
		// update full
		u3.setName("name");
		u3.setDisabled(false);
		restTemplate.put(PATH_DETAIL, u3, id);
		User u4 = restTemplate.getForEntity(PATH_DETAIL, User.class, id).getBody();
		assertThat(u4).isNotNull();
		assertThat(u4.getDisabled()).isEqualTo(u3.getDisabled());
		assertThat(u4.getName()).isEqualTo(u3.getName());
		assertThat(u4.getUsername()).isEqualTo(user.getUsername()); // username not updatable

		// delete
		assertThat(restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, id).build(), void.class)
				.getStatusCode()).isSameAs(BAD_REQUEST);
		u4.setDisabled(true);
		restTemplate.put(PATH_DETAIL, u4, id);
		assertThat(restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, id).build(), void.class)
				.getStatusCode()).isSameAs(OK);
		assertThat(restTemplate.getForEntity(PATH_DETAIL, User.class, id).getStatusCode()).isSameAs(NOT_FOUND);
	}

	@Test
	void list() {
		TestRestTemplate restTemplate = adminRestTemplate();
		ResponseEntity<ResultPage<User>> response = restTemplate.exchange(
				RequestEntity.method(GET, URI.create(PATH_LIST)).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		ResultPage<User> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult().size()).isEqualTo(2);
		assertThat(page.getPageNo()).isEqualTo(1);
		assertThat(page.getPageSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(2);
		assertThat(page.getResult().get(0).getCreatedDate()).isNull(); // User.View.List view

		response = restTemplate.exchange(RequestEntity.method(GET, URI.create(PATH_LIST + "?query=admin")).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult().size()).isEqualTo(1);
		assertThat(page.getPageNo()).isEqualTo(1);
		assertThat(page.getPageSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(1);

		ResponseEntity<ResultPage<User>> response2 = restTemplate.exchange(
				RequestEntity.method(GET, URI.create(PATH_LIST + "?username=admin")).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response2.getBody()).isEqualTo(response.getBody());
	}

	@Test
	void updatePassword() {
		TestRestTemplate restTemplate = adminRestTemplate();
		UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest();
		updatePasswordRequest.setPassword("iamtest");
		updatePasswordRequest.setConfirmedPassword("iamtest2");
		ResultPage<User> page = restTemplate
				.exchange(RequestEntity.method(GET, URI.create(PATH_LIST + "?query=admin")).build(),
						new ParameterizedTypeReference<ResultPage<User>>() {
						})
				.getBody();
		assertThat(page).isNotNull();
		User admin = page.getResult().get(0);
		ResponseEntity<?> response = restTemplate.exchange(
				RequestEntity.method(PUT, PATH_PASSWORD, admin.getId()).body(updatePasswordRequest), void.class);
		assertThat(response.getStatusCode()).isSameAs(BAD_REQUEST); // caused by wrong confirmed password

		updatePasswordRequest.setConfirmedPassword(updatePasswordRequest.getPassword());
		response = restTemplate.exchange(
				RequestEntity.method(PUT, PATH_PASSWORD, admin.getId()).body(updatePasswordRequest), void.class);
		assertThat(response.getStatusCode()).isSameAs(OK);

		response = restTemplate.exchange(RequestEntity.method(GET, URI.create(PATH_LIST)).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED); // caused by password changed

		restTemplate = restTemplate.withBasicAuth(ADMIN_USERNAME, updatePasswordRequest.getPassword());
		response = restTemplate.exchange(RequestEntity.method(GET, URI.create(PATH_LIST)).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);

		updatePasswordRequest.setPassword(DEFAULT_PASSWORD);
		updatePasswordRequest.setConfirmedPassword(updatePasswordRequest.getPassword());
		restTemplate.put(PATH_PASSWORD, updatePasswordRequest, admin.getId()); // change password back
	}

	@Test
	void conflictVersion() {
		TestRestTemplate restTemplate = adminRestTemplate();
		ResultPage<User> page = restTemplate.exchange(RequestEntity.method(GET, URI.create(PATH_LIST)).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				}).getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).isNotEmpty();
		User user = page.getResult().get(0);
		assertThat(user.getVersion()).isNotNull();
		user.setName(user.getName() + "2");
		user.setVersion(user.getVersion() + 1);
		ResponseEntity<User> response = restTemplate
				.exchange(RequestEntity.method(PATCH, PATH_DETAIL, user.getId()).body(user), User.class);
		assertThat(response.getStatusCode()).isSameAs(CONFLICT);
	}

}
