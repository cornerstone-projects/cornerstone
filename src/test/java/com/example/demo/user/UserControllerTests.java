package com.example.demo.user;

import static com.example.demo.MainApplication.ADMIN_USERNAME;
import static com.example.demo.MainApplication.DEFAULT_PASSWORD;
import static com.example.demo.user.UserController.PATH_DETAIL;
import static com.example.demo.user.UserController.PATH_LIST;
import static com.example.demo.user.UserController.PATH_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.OK;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.example.demo.ControllerTestBase;
import com.example.demo.core.hibernate.domain.ResultPage;

import lombok.Data;
import lombok.NoArgsConstructor;

class UserControllerTests extends ControllerTestBase {

	@Test
	void crud() {
		TestRestTemplate restTemplate = adminRestTemplate();
		User u = new User("test");
		u.setEnabled(true);
		ResponseEntity<User> response = restTemplate.postForEntity(PATH_LIST, u, User.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		User user = response.getBody();
		Long id = user.getId();
		assertThat(id).isNotNull();
		assertThat(user.getUsername()).isEqualTo(u.getUsername());
		assertThat(user.isEnabled()).isTrue();

		// get
		response = restTemplate.getForEntity(PATH_DETAIL, User.class, id);
		assertThat(response.getStatusCode()).isSameAs(OK);
		assertThat(response.getBody()).isEqualTo(user);

		// update
		User u2 = new User("other");
		u2.setEnabled(false);
		restTemplate.put(PATH_DETAIL, u2, id);
		User u3 = restTemplate.getForEntity(PATH_DETAIL, User.class, id).getBody();
		assertThat(u3.isEnabled()).isEqualTo(u2.isEnabled());
		assertThat(u3.getUsername()).isEqualTo(user.getUsername()); // username not updatable

		// delete
		restTemplate.delete(PATH_DETAIL, id);
		assertThat(restTemplate.getForEntity(PATH_DETAIL, User.class, id).getStatusCode()).isSameAs(NOT_FOUND);
	}

	@Test
	void list() {
		TestRestTemplate restTemplate = userRestTemplate();
		ResponseEntity<ResultPage<User>> response = restTemplate.exchange(
				RequestEntity.method(HttpMethod.GET, URI.create(PATH_LIST)).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		ResultPage<User> page = response.getBody();
		assertThat(page.getResult().size()).isEqualTo(2);
		assertThat(page.getPageNo()).isEqualTo(1);
		assertThat(page.getPageSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(2);

		response = restTemplate.exchange(
				RequestEntity.method(HttpMethod.GET, URI.create(PATH_LIST + "?query=adm")).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		page = response.getBody();
		assertThat(page.getResult().size()).isEqualTo(1);
		assertThat(page.getPageNo()).isEqualTo(1);
		assertThat(page.getPageSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(1);
	}

	@Test
	void changePassword() {
		TestRestTemplate restTemplate = adminRestTemplate();
		PasswordChangeRequest pcr = new PasswordChangeRequest();
		pcr.setPassword("iamtest");
		pcr.setConfirmedPassword("iamtest2");
		User admin = restTemplate
				.exchange(RequestEntity.method(HttpMethod.GET, URI.create(PATH_LIST + "?query=admin")).build(),
						new ParameterizedTypeReference<ResultPage<User>>() {
						})
				.getBody().getResult().get(0);
		ResponseEntity<?> response = restTemplate
				.exchange(RequestEntity.method(HttpMethod.PUT, PATH_PASSWORD, admin.getId()).body(pcr), void.class);
		assertThat(response.getStatusCode()).isNotSameAs(OK); // caused by wrong confirmed password

		pcr.setConfirmedPassword(pcr.getPassword());
		response = restTemplate.exchange(RequestEntity.method(HttpMethod.PUT, PATH_PASSWORD, admin.getId()).body(pcr),
				void.class);
		assertThat(response.getStatusCode()).isSameAs(OK);

		response = restTemplate.exchange(RequestEntity.method(HttpMethod.GET, URI.create(PATH_LIST)).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED); // caused by password changed

		restTemplate = restTemplate.withBasicAuth(ADMIN_USERNAME, pcr.getPassword());
		response = restTemplate.exchange(RequestEntity.method(HttpMethod.GET, URI.create(PATH_LIST)).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);

		pcr.setPassword(DEFAULT_PASSWORD);
		pcr.setConfirmedPassword(pcr.getPassword());
		restTemplate.put(PATH_PASSWORD, pcr, admin.getId()); // change password back
	}

	@Data
	@NoArgsConstructor
	static class User {

		private Long id;

		private String username;

		private String name;

		private String password;

		private boolean enabled;

		private Set<String> roles = new LinkedHashSet<>();

		public User(String username) {
			this.username = username;
			this.name = username;
		}

	}

}
