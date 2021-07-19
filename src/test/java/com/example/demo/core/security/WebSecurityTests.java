package com.example.demo.core.security;

import static com.example.demo.MainApplication.DEFAULT_PASSWORD;
import static com.example.demo.MainApplication.USER_USERNAME;
import static com.example.demo.core.security.WebSecurityTests.TEST_DEFAULT_SUCCESS_URL;
import static com.example.demo.core.security.WebSecurityTests.TEST_LOGIN_PAGE;
import static com.example.demo.core.security.WebSecurityTests.TEST_LOGIN_PROCESSING_URL;
import static com.example.demo.user.UserController.PATH_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.demo.ControllerTestBase;
import com.example.demo.user.User;

@TestPropertySource(properties = { "security.login-page=" + TEST_LOGIN_PAGE,
		"security.login-processing-url=" + TEST_LOGIN_PROCESSING_URL,
		"security.default-success-url=" + TEST_DEFAULT_SUCCESS_URL })
class WebSecurityTests extends ControllerTestBase {

	public static final String TEST_LOGIN_PROCESSING_URL = "/test";

	public static final String TEST_LOGIN_PAGE = "/test.html";

	public static final String TEST_DEFAULT_SUCCESS_URL = "/index.html";

	@Test
	void testAuthenticationFailure() {
		TestRestTemplate restTemplate = testRestTemplate;
		User u = new User();
		ResponseEntity<User> response = restTemplate.withBasicAuth("invalid_user", "*******").postForEntity(PATH_LIST,
				u, User.class);
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
	}

	@Test
	void testAccessDenied() {
		TestRestTemplate restTemplate = userRestTemplate();
		User u = new User();
		ResponseEntity<User> response = restTemplate.postForEntity(PATH_LIST, u, User.class);
		assertThat(response.getStatusCode()).isSameAs(FORBIDDEN);
	}

	@Test
	void testFormLoginFailure() {
		ResponseEntity<String> response = formLogin("invalid_user", "*******");
		assertThat(response.getStatusCode()).isSameAs(FOUND);
		assertThat(response.getHeaders().getLocation()).hasPath(TEST_LOGIN_PAGE).hasQuery("error");
	}

	@Test
	void testFormLoginSuccess() {
		ResponseEntity<String> response = formLogin(USER_USERNAME, DEFAULT_PASSWORD);
		assertThat(response.getStatusCode()).isSameAs(FOUND);
		assertThat(response.getHeaders().getLocation()).hasPath(TEST_DEFAULT_SUCCESS_URL);
	}

	@Test
	void testRestfulFormLoginFailure() {
		ResponseEntity<Map<String, Object>> response = restfulFormLogin("invalid_user", "*******");
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
		assertThat(response.getBody().get("status")).isEqualTo(UNAUTHORIZED.value());
		assertThat(response.getBody().get("message")).isNotEqualTo(UNAUTHORIZED.getReasonPhrase());
		assertThat(response.getBody().get("path")).isEqualTo(TEST_LOGIN_PROCESSING_URL);
	}

	@Test
	void testRestfulFormLoginSuccess() {
		ResponseEntity<Map<String, Object>> response = restfulFormLogin(USER_USERNAME, DEFAULT_PASSWORD);
		assertThat(response.getStatusCode()).isSameAs(OK);
		assertThat(response.getBody().get("status")).isEqualTo(OK.value());
		assertThat(response.getBody().get("message")).isEqualTo(OK.getReasonPhrase());
		assertThat(response.getBody().get("path")).isEqualTo(TEST_LOGIN_PROCESSING_URL);
	}

	@Test
	void testAccessWithUnauthenticated() {
		ResponseEntity<String> response = executeWithNoRedirects(template -> template.exchange(
				RequestEntity.method(POST, URI.create(testRestTemplate.getRootUri() + TEST_DEFAULT_SUCCESS_URL))
						.header(ACCEPT, TEXT_HTML_VALUE).build(),
				String.class));
		// GET always follow redirects
		assertThat(response.getStatusCode()).isSameAs(FOUND);
		assertThat(response.getHeaders().getLocation()).hasPath(TEST_LOGIN_PAGE);
	}

	@Test
	void testRestfulAccessWithUnauthenticated() {
		ResponseEntity<Map<String, Object>> response = executeWithNoRedirects(
				template -> template
						.exchange(
								RequestEntity
										.method(POST,
												URI.create(testRestTemplate.getRootUri() + TEST_DEFAULT_SUCCESS_URL))
										.header(ACCEPT, APPLICATION_JSON_VALUE).build(),
								new ParameterizedTypeReference<Map<String, Object>>() {
								}));
		assertThat(response.getStatusCode()).isSameAs(FORBIDDEN);
		assertThat(response.getBody().get("status")).isEqualTo(FORBIDDEN.value());
		assertThat(response.getBody().get("message")).isEqualTo("Access Denied");
		assertThat(response.getBody().get("path")).isEqualTo(TEST_DEFAULT_SUCCESS_URL);
	}

	private ResponseEntity<Map<String, Object>> restfulFormLogin(String username, String password) {
		Map<String, String> data = new LinkedHashMap<>();
		data.put("username", username);
		data.put("password", password);
		return testRestTemplate.exchange(RequestEntity.method(POST, URI.create(TEST_LOGIN_PROCESSING_URL)).body(data),
				new ParameterizedTypeReference<Map<String, Object>>() {
				});
	}

	private ResponseEntity<String> formLogin(String username, String password) {
		MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
		data.add("username", username);
		data.add("password", password);
		return executeWithNoRedirects(
				template -> template
						.exchange(
								RequestEntity
										.method(POST,
												URI.create(testRestTemplate.getRootUri() + TEST_LOGIN_PROCESSING_URL))
										.header(ACCEPT, TEXT_HTML_VALUE)
										.header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE).body(data),
								String.class));
	}

	private <T> T executeWithNoRedirects(Function<RestTemplate, T> function) {
		// disable follow redirects
		HttpURLConnection.setFollowRedirects(false);
		try {
			RestTemplate template = new RestTemplate(new SimpleClientHttpRequestFactory());
			template.setErrorHandler(testRestTemplate.getRestTemplate().getErrorHandler());
			return function.apply(template);
		} finally {
			HttpURLConnection.setFollowRedirects(true); // restore defaults
		}
	}
}
