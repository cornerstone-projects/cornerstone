package io.cornerstone.core.security.password;

import java.net.HttpURLConnection;
import java.util.function.Function;
import java.util.stream.Stream;

import io.cornerstone.test.ControllerTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static io.cornerstone.core.security.SecurityProperties.DEFAULT_LOGIN_PROCESSING_URL;
import static io.cornerstone.core.security.SecurityProperties.DEFAULT_SUCCESS_URL;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.FOUND;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@ContextConfiguration(classes = MixedPasswordEncoderTests.Config.class)
class MixedPasswordEncoderTests extends ControllerTestBase {

	@Test
	void testFormLoginSuccessWithShaPassword() {
		String shaPassword = new String(Hex.encode(MessageDigestUtils.sha(DEFAULT_PASSWORD.getBytes())));
		ResponseEntity<String> response = formLogin(USER_USERNAME, shaPassword);
		assertThat(response.getStatusCode()).isSameAs(FOUND);
		assertThat(response.getHeaders().getLocation()).hasPath(DEFAULT_SUCCESS_URL);
	}

	private ResponseEntity<String> formLogin(String username, String password) {
		MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
		data.add("username", username);
		data.add("password", password);
		return executeWithNoRedirects(template -> template
			.exchange(RequestEntity.method(POST, this.testRestTemplate.getRootUri() + DEFAULT_LOGIN_PROCESSING_URL)
				.header(ACCEPT, TEXT_HTML_VALUE)
				.header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.body(data), String.class));
	}

	private <T> T executeWithNoRedirects(Function<RestTemplate, T> function) {
		// disable follow redirects
		HttpURLConnection.setFollowRedirects(false);
		try {
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setOutputStreaming(false);
			RestTemplate template = new RestTemplate(requestFactory);
			template.setErrorHandler(this.testRestTemplate.getRestTemplate().getErrorHandler());
			return function.apply(template);
		}
		finally {
			HttpURLConnection.setFollowRedirects(true); // restore defaults
		}
	}

	@ComponentScan
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
