package io.cornerstone.core.security;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import io.cornerstone.test.ControllerTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.cornerstone.core.security.WebSecurityTests.TEST_DEFAULT_SUCCESS_URL;
import static io.cornerstone.core.security.WebSecurityTests.TEST_LOGIN_PAGE;
import static io.cornerstone.core.security.WebSecurityTests.TEST_LOGIN_PROCESSING_URL;
import static java.util.stream.Collectors.toList;
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

@TestPropertySource(properties = { "security.login-page=" + TEST_LOGIN_PAGE,
		"security.login-processing-url=" + TEST_LOGIN_PROCESSING_URL,
		"security.default-success-url=" + TEST_DEFAULT_SUCCESS_URL,
		"security.authorize-requests-mapping[/admin/**]=ADMIN" })
@ContextConfiguration
class WebSecurityTests extends ControllerTestBase {

	static final String TEST_LOGIN_PROCESSING_URL = "/test";

	static final String TEST_LOGIN_PAGE = "/test.html";

	static final String TEST_DEFAULT_SUCCESS_URL = "/index.html";

	static final String TEST_USER_HOME = "/user/home.html";

	static final String TEST_ADMIN_HOME = "/admin/home.html";

	static final String TEST_IGNORING_PATH = "/ignoring";

	static final String TEST_PERMIT_ALL_PATH = "/permitAll";

	@Test
	void testAuthenticationFailure() {
		TestRestTemplate restTemplate = this.testRestTemplate;
		ResponseEntity<String> response = restTemplate.withBasicAuth("invalid_user", "*******")
			.getForEntity(TEST_DEFAULT_SUCCESS_URL, String.class);
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
	}

	@Test
	void testAccessDenied() {
		TestRestTemplate restTemplate = userRestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(TEST_ADMIN_HOME, String.class);
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
		assertThat(response.getBody().get("targetUrl")).isEqualTo(TEST_DEFAULT_SUCCESS_URL);
	}

	@Test
	void testAccessWithUnauthenticated() {
		ResponseEntity<String> response = this.testRestTemplate.exchange(
				RequestEntity.method(POST, TEST_DEFAULT_SUCCESS_URL).header(ACCEPT, TEXT_HTML_VALUE).build(),
				String.class);
		// GET always follow redirects
		assertThat(response.getStatusCode()).isSameAs(FOUND);
		assertThat(response.getHeaders().getLocation()).hasPath(TEST_LOGIN_PAGE);
	}

	@Test
	void testRestfulAccessWithUnauthenticated() {
		ResponseEntity<Map<String, Object>> response = this.testRestTemplate.exchange(
				RequestEntity.method(POST, TEST_DEFAULT_SUCCESS_URL).header(ACCEPT, APPLICATION_JSON_VALUE).build(),
				new ParameterizedTypeReference<>() {
				});
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
		assertThat(response.getBody().get("status")).isEqualTo(UNAUTHORIZED.value());
		assertThat(response.getBody().get("message")).isEqualTo(this.messageSource.getMessage(
				"ExceptionTranslationFilter.insufficientAuthentication", null, LocaleContextHolder.getLocale()));
		assertThat(response.getBody().get("path")).isEqualTo(TEST_DEFAULT_SUCCESS_URL);
	}

	@Test
	void testAuthorizeRequestsMapping() {
		ResponseEntity<String> response = userRestTemplate().getForEntity(TEST_ADMIN_HOME, String.class);
		assertThat(response.getStatusCode()).isSameAs(FORBIDDEN);
		response = adminRestTemplate().getForEntity(TEST_ADMIN_HOME, String.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
	}

	@Test
	void testUsernameAndTypeMapper() {
		ResponseEntity<String> response = userRestTemplate().getForEntity(TEST_DEFAULT_SUCCESS_URL, String.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		response = userRestTemplate().getForEntity(TEST_USER_HOME, String.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		response = adminRestTemplate().getForEntity(TEST_USER_HOME, String.class);
		assertThat(response.getStatusCode()).isSameAs(FORBIDDEN);
	}

	@Test
	void testIgnoredRequestContributor() {
		ResponseEntity<String> response = this.testRestTemplate.getForEntity(TEST_IGNORING_PATH, String.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
	}

	@Test
	void testPermitAllRequestContributor() {
		ResponseEntity<String> response = this.testRestTemplate.getForEntity(TEST_PERMIT_ALL_PATH, String.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
	}

	private ResponseEntity<Map<String, Object>> restfulFormLogin(String username, String password) {
		Map<String, String> data = new LinkedHashMap<>();
		data.put("username", username);
		data.put("password", password);
		return this.testRestTemplate.exchange(RequestEntity.method(POST, TEST_LOGIN_PROCESSING_URL).body(data),
				new ParameterizedTypeReference<>() {
				});
	}

	private ResponseEntity<String> formLogin(String username, String password) {
		MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
		data.add("username", username);
		data.add("password", password);
		return this.testRestTemplate.exchange(RequestEntity.method(POST, TEST_LOGIN_PROCESSING_URL)
			.header(ACCEPT, TEXT_HTML_VALUE)
			.header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
			.body(data), String.class);
	}

	@RestController
	static class TestController {

		@GetMapping(TEST_DEFAULT_SUCCESS_URL)
		@Secured("USER")
		String home() {
			return "home";
		}

		@GetMapping(TEST_USER_HOME)
		@Secured("USERNAME(user)")
		String user() {
			return "user";
		}

		@GetMapping(TEST_ADMIN_HOME)
		String admin() {
			return "admin";
		}

	}

	@RestController
	static class TestIgnoredRequestController implements IgnoringRequestContributor {

		@GetMapping(TEST_IGNORING_PATH)
		String ignoring() {
			return "ignoring";
		}

		@Override
		public String getIgnoringPathPattern() {
			return TEST_IGNORING_PATH;
		}

	}

	@RestController
	static class TestPermitAllRequestController implements PermitAllRequestContributor {

		@GetMapping(TEST_PERMIT_ALL_PATH)
		String permitAll() {
			return "permitAll";
		}

		@Override
		public String getPermitAllPathPattern() {
			return TEST_PERMIT_ALL_PATH;
		}

	}

	@ComponentScan
	@Configuration
	static class Config {

		@Bean
		UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
			return new InMemoryUserDetailsManager(createUser(USER_USERNAME, passwordEncoder.encode(DEFAULT_PASSWORD)),
					createUser(ADMIN_USERNAME, passwordEncoder.encode(DEFAULT_PASSWORD), ADMIN_ROLE));
		}

		private static User createUser(String username, String password, String... roles) {
			return new User(username, password, Stream.of(roles).map(SimpleGrantedAuthority::new).collect(toList()));
		}

	}

}
