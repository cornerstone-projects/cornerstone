package io.cornerstone.core.security;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import io.cornerstone.test.ControllerTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static io.cornerstone.core.security.SecurityProperties.DEFAULT_LOGIN_PROCESSING_URL;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@TestPropertySource(
		properties = { "security.authentication.username-max-length=12", "security.authentication.max-attempts=3" })
@ContextConfiguration
class AttackProtectionTests extends ControllerTestBase {

	@Autowired
	MessageSource messageSource;

	@MockBean
	StringRedisTemplate stringRedisTemplate;

	@SpyBean
	UserDetailsService userDetailsService;

	@Test
	void testDOS() {

		@SuppressWarnings("unchecked")
		ValueOperations<String, String> opsForValue = mock(ValueOperations.class);
		given(this.stringRedisTemplate.opsForValue()).willReturn(opsForValue);

		String username = "abcdedfhijklm";
		ResponseEntity<Map<String, Object>> response = login(username, DEFAULT_PASSWORD);
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
		assertThat(response.getBody().get("status")).isEqualTo(UNAUTHORIZED.value());
		assertThat(response.getBody().get("message")).isEqualTo(this.messageSource.getMessage("JdbcDaoImpl.notFound",
				new Object[] { username }, "Username {0} not found", Locale.getDefault()));
		assertThat(response.getBody().get("path")).isEqualTo(DEFAULT_LOGIN_PROCESSING_URL);
		then(this.userDetailsService).shouldHaveNoInteractions();

		username = "abcdedfhijkl";
		response = login(username, DEFAULT_PASSWORD);
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
		assertThat(response.getBody().get("status")).isEqualTo(UNAUTHORIZED.value());
		assertThat(response.getBody().get("message")).isNotEqualTo(this.messageSource.getMessage("JdbcDaoImpl.notFound",
				new Object[] { username }, "Username {0} not found", Locale.getDefault()));
		assertThat(response.getBody().get("path")).isEqualTo(DEFAULT_LOGIN_PROCESSING_URL);
		then(this.userDetailsService).should().loadUserByUsername(username);
	}

	@Test
	void testBruteForce() {
		String username = ADMIN_USERNAME;
		Long maxAttempts = 3L;
		String key = "fla:" + username;
		String message = this.messageSource.getMessage("DefaultAuthenticationManager.maxAttemptsExceeded",
				new Object[] { maxAttempts }, "Login attempts exceed {0}", Locale.getDefault());
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> opsForValue = mock(ValueOperations.class);
		given(this.stringRedisTemplate.opsForValue()).willReturn(opsForValue);
		given(opsForValue.increment(key, 0)).willReturn(maxAttempts - 1);
		given(opsForValue.increment(key)).willReturn(maxAttempts);

		ResponseEntity<Map<String, Object>> response = login(username, "********");
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
		assertThat(response.getBody().get("message")).isNotEqualTo(message);
		then(opsForValue).should().increment(key);

		given(opsForValue.increment(key, 0)).willReturn(maxAttempts);
		response = login(username, "********");
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
		assertThat(response.getBody().get("message")).isEqualTo(message);
	}

	private ResponseEntity<Map<String, Object>> login(String username, String password) {
		Map<String, String> data = new LinkedHashMap<>();
		data.put("username", username);
		data.put("password", password);
		return this.testRestTemplate.exchange(RequestEntity.method(POST, DEFAULT_LOGIN_PROCESSING_URL).body(data),
				new ParameterizedTypeReference<>() {
				});
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
