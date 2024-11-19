package io.cornerstone.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.test.ControllerTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

class UserControllerTests extends ControllerTestBase {

	private static final String PATH_LIST = "/users";

	private static final String PATH_DETAIL = "/user/{id}";

	private static final String PATH_PASSWORD = PATH_DETAIL + "/password";

	@Test
	void crud() {
		TestRestTemplate restTemplate = adminRestTemplate();
		User u = new User();
		u.setName("test");
		u.setDisabled(true);

		// create
		u.setUsername(ADMIN_USERNAME);
		ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(RequestEntity.method(POST, PATH_LIST).body(u),
				new ParameterizedTypeReference<>() {
				});
		assertThat(resp.getStatusCode()).isSameAs(BAD_REQUEST);
		assertThat(resp.getBody()
			.get(resp.getHeaders().getContentType().isCompatibleWith(APPLICATION_PROBLEM_JSON) ? "detail" : "message"))
			.isEqualTo(this.messageSource.getMessage("username.already.exists", null, LocaleContextHolder.getLocale()));
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
		assertThat(u3.getUsername()).isEqualTo(user.getUsername()); // username not
																	// updatable
		// update full
		u3.setName("name");
		u3.setDisabled(false);
		restTemplate.put(PATH_DETAIL, u3, id);
		User u4 = restTemplate.getForEntity(PATH_DETAIL, User.class, id).getBody();
		assertThat(u4).isNotNull();
		assertThat(u4.getDisabled()).isEqualTo(u3.getDisabled());
		assertThat(u4.getName()).isEqualTo(u3.getName());
		assertThat(u4.getUsername()).isEqualTo(user.getUsername()); // username not
																	// updatable

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
		ResponseEntity<ResultPage<User>> response = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST).build(),
				new ParameterizedTypeReference<>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		ResultPage<User> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(2);
		assertThat(page.getPage()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(2);
		assertThat(page.getResult().getFirst().getCreatedDate()).isNull(); // User.View.List
																			// view
		response = restTemplate.exchange(
				RequestEntity.method(GET, PATH_LIST + "?page=2&size=1&sort=username,desc").build(),
				new ParameterizedTypeReference<>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(1);
		assertThat(page.getPage()).isEqualTo(2);
		assertThat(page.getSize()).isEqualTo(1);
		assertThat(page.getResult().getFirst().getUsername()).isEqualTo(ADMIN_USERNAME);

		response = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?query=admin").build(),
				new ParameterizedTypeReference<>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);
		page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(1);
		assertThat(page.getPage()).isEqualTo(1);
		assertThat(page.getSize()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(1);
		assertThat(page.getTotalElements()).isEqualTo(1);

		ResponseEntity<ResultPage<User>> response2 = restTemplate.exchange(
				RequestEntity.method(GET, PATH_LIST + "?username=admin").build(), new ParameterizedTypeReference<>() {
				});
		assertThat(response2.getBody()).isEqualTo(response.getBody());
	}

	@Test
	void updatePassword() {
		TestRestTemplate restTemplate = adminRestTemplate();
		UpdatePasswordRequest updatePasswordRequest = new UpdatePasswordRequest();
		updatePasswordRequest.setPassword("iamtest");
		updatePasswordRequest.setConfirmedPassword("iamtest2");
		ResultPage<User> page = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST + "?query=admin").build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				})
			.getBody();
		assertThat(page).isNotNull();
		User admin = page.getResult().getFirst();
		ResponseEntity<?> response = restTemplate
			.exchange(RequestEntity.method(PUT, PATH_PASSWORD, admin.getId()).body(updatePasswordRequest), void.class);
		assertThat(response.getStatusCode()).isSameAs(BAD_REQUEST); // caused by wrong
																	// confirmed password

		updatePasswordRequest.setConfirmedPassword(updatePasswordRequest.getPassword());
		response = restTemplate
			.exchange(RequestEntity.method(PUT, PATH_PASSWORD, admin.getId()).body(updatePasswordRequest), void.class);
		assertThat(response.getStatusCode()).isSameAs(OK);

		response = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED); // caused by password
																		// changed

		restTemplate = restTemplate.withBasicAuth(ADMIN_USERNAME, updatePasswordRequest.getPassword());
		response = restTemplate.exchange(RequestEntity.method(GET, PATH_LIST).build(),
				new ParameterizedTypeReference<ResultPage<User>>() {
				});
		assertThat(response.getStatusCode()).isSameAs(OK);

		updatePasswordRequest.setPassword(DEFAULT_PASSWORD);
		updatePasswordRequest.setConfirmedPassword(updatePasswordRequest.getPassword());
		restTemplate.put(PATH_PASSWORD, updatePasswordRequest, admin.getId());
	}

	@Test
	void conflictVersion() {
		TestRestTemplate restTemplate = adminRestTemplate();
		ResultPage<User> page = restTemplate
			.exchange(RequestEntity.method(GET, PATH_LIST).build(), new ParameterizedTypeReference<ResultPage<User>>() {
			})
			.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).isNotEmpty();
		User user = page.getResult().getFirst();
		assertThat(user.getVersion()).isNotNull();
		user.setName(user.getName() + "2");
		user.setVersion(user.getVersion() + 1);
		ResponseEntity<User> response = restTemplate
			.exchange(RequestEntity.method(PATCH, PATH_DETAIL, user.getId()).body(user), User.class);
		assertThat(response.getStatusCode()).isSameAs(CONFLICT);
	}

	@Test
	void download() throws IOException {
		TestRestTemplate restTemplate = adminRestTemplate();
		int size = 10;
		for (int i = 0; i < size; i++) {
			User u = new User();
			u.setUsername("test" + i);
			u.setName("test");
			u.setDisabled(i % 2 == 0);
			restTemplate.postForObject(PATH_LIST, u, void.class);
		}
		ResponseEntity<Resource> response = restTemplate.getForEntity(PATH_LIST + ".csv?sort=createdDate",
				Resource.class);
		assertThat(response.getHeaders().getContentType().getSubtype()).isEqualTo("csv");
		assertThat(response.getStatusCode()).isSameAs(OK);
		Resource resource = response.getBody();
		assertThat(resource).isNotNull();
		List<String> ids = new ArrayList<>();
		try (InputStream is = resource.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			List<String> lines = reader.lines().toList();
			assertThat(lines).hasSize(size + 3);
			assertThat(lines).element(4).asString().contains(",test,");
			for (int i = 3; i < (size + 3); i++) {
				ids.add(lines.get(i).split(",")[0]);
			}
		}

		response = restTemplate.getForEntity(PATH_LIST + ".csv?sort=createdDate&disabled=true", Resource.class);
		try (InputStream is = response.getBody().getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			List<String> lines = reader.lines().toList();
			assertThat(lines).hasSize(size / 2 + 1);
		}

		ids.forEach(id -> restTemplate.delete(PATH_DETAIL, id));
	}

	@Test
	void upload() {
		TestRestTemplate restTemplate = adminRestTemplate();
		String body = "test1,xxx,13111111111,A B C,true\ntest2,xxx,13222222222,,true";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("text/csv"));
		HttpEntity<String> entity = new HttpEntity<>(body, headers);
		assertThat(restTemplate.postForEntity(PATH_LIST + ".csv", entity, void.class).getStatusCode()).isSameAs(OK);

		ResponseEntity<ResultPage<User>> response = restTemplate
			.exchange(RequestEntity.method(GET, PATH_LIST + "?name=xxx").build(), new ParameterizedTypeReference<>() {
			});
		assertThat(response.getStatusCode()).isSameAs(OK);
		ResultPage<User> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.getResult()).hasSize(2);
		assertThat(page.getResult().getFirst().getUsername()).isEqualTo("test1");
		assertThat(page.getResult().getFirst().getRoles()).containsExactly("A", "B", "C");
		assertThat(page.getResult().getFirst().getDisabled()).isSameAs(Boolean.TRUE);
		assertThat(page.getResult().get(1).getUsername()).isEqualTo("test2");
		assertThat(page.getResult().get(1).getRoles()).isNullOrEmpty();
		assertThat(page.getResult().get(1).getDisabled()).isSameAs(Boolean.TRUE);
		page.getResult().forEach(u -> restTemplate.delete(PATH_DETAIL, u.getId()));
	}

	@Test
	void testForbidden() {
		TestRestTemplate restTemplate = adminRestTemplate();
		ResultPage<User> page = restTemplate
			.exchange(RequestEntity.method(GET, PATH_LIST).build(), new ParameterizedTypeReference<ResultPage<User>>() {
			})
			.getBody();
		assertThat(page).isNotNull();
		User user = page.getResult().getFirst();

		restTemplate = userRestTemplate();
		assertThat(restTemplate.getForEntity(PATH_LIST, String.class).getStatusCode()).isSameAs(FORBIDDEN);
		assertThat(restTemplate.getForEntity(PATH_DETAIL, String.class, user.getId()).getStatusCode())
			.isSameAs(FORBIDDEN);
		User u = new User();
		u.setName("temp");
		u.setDisabled(true);
		assertThat(restTemplate.postForEntity(PATH_LIST, u, User.class).getStatusCode()).isSameAs(FORBIDDEN);
		user.setName("new name");
		assertThat(restTemplate.exchange(RequestEntity.method(PUT, PATH_DETAIL, user.getId()).body(user), String.class)
			.getStatusCode()).isSameAs(FORBIDDEN);
		assertThat(
				restTemplate.exchange(RequestEntity.method(PATCH, PATH_DETAIL, user.getId()).body(user), String.class)
					.getStatusCode())
			.isSameAs(FORBIDDEN);
		assertThat(restTemplate.exchange(RequestEntity.method(DELETE, PATH_DETAIL, user.getId()).build(), String.class)
			.getStatusCode()).isSameAs(FORBIDDEN);
	}

}
