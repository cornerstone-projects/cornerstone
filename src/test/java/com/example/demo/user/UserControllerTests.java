package com.example.demo.user;

import static com.example.demo.MainApplication.ADMIN_USERNAME;
import static com.example.demo.MainApplication.DEFAULT_PASSWORD;
import static com.example.demo.user.UserController.PATH_DETAIL;
import static com.example.demo.user.UserController.PATH_LIST;
import static com.example.demo.user.UserController.PATH_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
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
		u.setUsername("test");
		u.setName("test");
		u.setDisabled(true);
		ResponseEntity<User> response = restTemplate.postForEntity(PATH_LIST, u, User.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		User user = response.getBody();
		Long id = user.getId();
		assertThat(id).isNotNull();
		assertThat(user.getUsername()).isEqualTo(u.getUsername());
		assertThat(user.getDisabled()).isEqualTo(u.getDisabled());

		// get
		response = restTemplate.getForEntity(PATH_DETAIL, User.class, id);
		assertThat(response.getStatusCode()).isSameAs(OK);
		assertThat(response.getBody()).isEqualTo(user);

		// partial update
		User u2 = new User();
		u2.setUsername("other");
		u2.setName("new name");
		u2.setDisabled(false);
		User u3 = restTemplate.patchForObject(PATH_DETAIL, u2, User.class, id);
		assertThat(u3.getDisabled()).isEqualTo(u2.getDisabled());
		assertThat(u3.getUsername()).isEqualTo(user.getUsername()); // username not updatable
		// full update
		u3.setName("name");
		u3.setDisabled(true);
		restTemplate.put(PATH_DETAIL, u3, id);
		User u4 = restTemplate.getForEntity(PATH_DETAIL, User.class, id).getBody();
		assertThat(u4.getDisabled()).isEqualTo(u3.getDisabled());
		assertThat(u4.getName()).isEqualTo(u3.getName());
		assertThat(u4.getUsername()).isEqualTo(user.getUsername()); // username not updatable

		// delete
		restTemplate.delete(PATH_DETAIL, id);
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
		assertThat(page.getResult().size()).isEqualTo(2);
		assertThat(page.getPageNo()).isEqualTo(1);
		assertThat(page.getPageSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(2);

		response = restTemplate.exchange(RequestEntity.method(GET, URI.create(PATH_LIST + "?query=admin")).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		page = response.getBody();
		assertThat(page.getResult().size()).isEqualTo(1);
		assertThat(page.getPageNo()).isEqualTo(1);
		assertThat(page.getPageSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(1);
		
		ResponseEntity<ResultPage<User>> response2 = restTemplate.exchange(RequestEntity.method(GET, URI.create(PATH_LIST + "?username=admin")).build(),
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
		User admin = restTemplate.exchange(RequestEntity.method(GET, URI.create(PATH_LIST + "?query=admin")).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				}).getBody().getResult().get(0);
		ResponseEntity<?> response = restTemplate.exchange(
				RequestEntity.method(PUT, PATH_PASSWORD, admin.getId()).body(updatePasswordRequest), void.class);
		assertThat(response.getStatusCode()).isNotSameAs(OK); // caused by wrong confirmed password

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
		User user = restTemplate.exchange(RequestEntity.method(GET, URI.create(PATH_LIST)).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				}).getBody().getResult().get(0);
		assertThat(user.getVersion()).isNotNull();
		user.setName(user.getName() + "2");
		user.setVersion(user.getVersion() + 1);
		ResponseEntity<User> response = restTemplate
				.exchange(RequestEntity.method(PATCH, PATH_DETAIL, user.getId()).body(user), User.class);
		assertThat(response.getStatusCode()).isSameAs(CONFLICT);
	}

}
