package io.cornerstone.core.security;

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

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
import org.springframework.web.client.RestTemplate;

import io.cornerstone.test.ControllerTestBase;
import io.vavr.collection.Stream;

@TestPropertySource(properties = { "security.login-page=" + TEST_LOGIN_PAGE,
		"security.login-processing-url=" + TEST_LOGIN_PROCESSING_URL,
		"security.default-success-url=" + TEST_DEFAULT_SUCCESS_URL,
		"security.authorize-requests-mapping[/admin/**]=ADMIN" })
@ContextConfiguration(classes = WebSecurityTests.Config.class)
class WebSecurityTests extends ControllerTestBase {

	public static final String TEST_LOGIN_PROCESSING_URL = "/test";

	public static final String TEST_LOGIN_PAGE = "/test.html";

	public static final String TEST_DEFAULT_SUCCESS_URL = "/index.html";

	public static final String TEST_ADMIN_HOME = "/admin/home.html";

	@Test
	void testAuthenticationFailure() {
		TestRestTemplate restTemplate = testRestTemplate;
		ResponseEntity<String> response = restTemplate.withBasicAuth("invalid_user", "*******")
				.getForEntity(TEST_DEFAULT_SUCCESS_URL, String.class);
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
	}

	@Test
	void testAccessDenied() {
		TestRestTemplate restTemplate = userRestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(TEST_DEFAULT_SUCCESS_URL, String.class);
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
		assertThat(response.getBody().get("targetUrl")).isEqualTo(TEST_DEFAULT_SUCCESS_URL);
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
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
		assertThat(response.getBody().get("status")).isEqualTo(UNAUTHORIZED.value());
		assertThat(response.getBody().get("message")).isEqualTo(messageSource.getMessage(
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
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setOutputStreaming(false);
			RestTemplate template = new RestTemplate(requestFactory);
			template.setErrorHandler(testRestTemplate.getRestTemplate().getErrorHandler());
			return function.apply(template);
		} finally {
			HttpURLConnection.setFollowRedirects(true); // restore defaults
		}
	}

	@RestController
	@Secured(ADMIN_ROLE)
	static class TestController {

		@GetMapping(TEST_DEFAULT_SUCCESS_URL)
		public String get() {
			return "test";
		}

		@GetMapping(TEST_ADMIN_HOME)
		public String home() {
			return "home";
		}
	}

	@ComponentScan
	static class Config {

		@Bean
		AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
			// see InitializeAuthenticationProviderManagerConfigurer.getBeanOrNull()
			UserDetailsService uds = new InMemoryUserDetailsManager(
					createUser(USER_USERNAME, passwordEncoder.encode(DEFAULT_PASSWORD)),
					createUser(ADMIN_USERNAME, passwordEncoder.encode(DEFAULT_PASSWORD), ADMIN_ROLE));
			DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
			provider.setUserDetailsService(uds);
			provider.setPasswordEncoder(passwordEncoder);
			return provider;
		}

		private User createUser(String username, String password, String... roles) {
			return new User(username, password,
					Stream.of(roles).map(r -> new SimpleGrantedAuthority(r)).collect(toList()));
		}
	}
}