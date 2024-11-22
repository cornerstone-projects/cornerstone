package io.cornerstone.core.security;

import java.time.Duration;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import io.cornerstone.core.security.verification.VerificationAware;
import io.cornerstone.core.security.verification.VerificationCodeNotifier;
import io.cornerstone.core.security.verification.VerificationCodeRequirement;
import io.cornerstone.core.security.verification.impl.DefaultVerificationService;
import io.cornerstone.test.ControllerTestBase;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static io.cornerstone.core.security.SecurityProperties.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.*;

@TestPropertySource(properties = { "verification.code.enabled=true" })
@ContextConfiguration
class LoginWithVerificationCodeTests extends ControllerTestBase {

	@MockitoBean
	private StringRedisTemplate stringRedisTemplate;

	@Captor
	ArgumentCaptor<String> verificationCodeCaptor;

	@Test
	void testFormLoginFailure() {
		String username = ADMIN_USERNAME;
		String key = DefaultVerificationService.CACHE_NAMESPACE + username;
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> opsForValue = mock(ValueOperations.class);
		given(opsForValue.get(key)).willReturn("");
		given(this.stringRedisTemplate.opsForValue()).willReturn(opsForValue);

		ResponseEntity<String> response = formLogin(username, DEFAULT_PASSWORD, "123456");
		assertThat(response.getStatusCode()).isSameAs(FOUND);
		assertThat(response.getHeaders().getLocation()).hasPath(DEFAULT_LOGIN_PAGE).hasQuery("error");
	}

	@Test
	void testFormLoginSuccess() {
		String username = ADMIN_USERNAME;
		String key = DefaultVerificationService.CACHE_NAMESPACE + username;
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> opsForValue = mock(ValueOperations.class);
		given(this.stringRedisTemplate.opsForValue()).willReturn(opsForValue);

		VerificationCodeRequirement requirement = this.testRestTemplate.getForObject("/verificationCode/" + username,
				VerificationCodeRequirement.class);
		assertThat(requirement.isRequired()).isTrue();
		assertThat(requirement.getSendingRequired()).isEqualTo(Boolean.TRUE);

		ResponseEntity<String> response = this.testRestTemplate.postForEntity("/verificationCode/" + username, null,
				String.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		then(opsForValue).should().set(eq(key), this.verificationCodeCaptor.capture(), any(Duration.class));

		String verificationCode = this.verificationCodeCaptor.getValue();

		given(opsForValue.get(key)).willReturn(verificationCode);
		response = formLogin(username, DEFAULT_PASSWORD, verificationCode);
		assertThat(response.getStatusCode()).isSameAs(FOUND);
		assertThat(response.getHeaders().getLocation()).hasPath(DEFAULT_SUCCESS_URL);
	}

	@Test
	void testFormLoginWithoutPassword() {
		String username = USER_USERNAME;
		String key = DefaultVerificationService.CACHE_NAMESPACE + username;
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> opsForValue = mock(ValueOperations.class);
		given(this.stringRedisTemplate.opsForValue()).willReturn(opsForValue);

		VerificationCodeRequirement requirement = this.testRestTemplate.getForObject("/verificationCode/" + username,
				VerificationCodeRequirement.class);
		assertThat(requirement.isRequired()).isTrue();
		assertThat(requirement.getSendingRequired()).isEqualTo(Boolean.TRUE);
		assertThat(requirement.getPasswordHidden()).isEqualTo(Boolean.TRUE);

		ResponseEntity<String> response = this.testRestTemplate.postForEntity("/verificationCode/" + username, null,
				String.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		then(opsForValue).should().set(eq(key), this.verificationCodeCaptor.capture(), any(Duration.class));
		String verificationCode = this.verificationCodeCaptor.getValue();
		given(opsForValue.get(key)).willReturn(verificationCode);
		response = formLogin(username, null, verificationCode);
		assertThat(response.getStatusCode()).isSameAs(FOUND);
		assertThat(response.getHeaders().getLocation()).hasPath(DEFAULT_SUCCESS_URL);

		// verificationCode instead of password
		response = this.testRestTemplate.postForEntity("/verificationCode/" + username, null, String.class);
		assertThat(response.getStatusCode()).isSameAs(OK);
		then(opsForValue).should().set(eq(key), this.verificationCodeCaptor.capture(), any(Duration.class));
		verificationCode = this.verificationCodeCaptor.getValue();
		given(opsForValue.get(key)).willReturn(verificationCode);
		response = formLogin(username, verificationCode, null);
		assertThat(response.getStatusCode()).isSameAs(FOUND);
		assertThat(response.getHeaders().getLocation()).hasPath(DEFAULT_SUCCESS_URL);
	}

	@Test
	void testRestfulFormLoginFailure() {
		String username = ADMIN_USERNAME;
		String key = DefaultVerificationService.CACHE_NAMESPACE + username;
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> opsForValue = mock(ValueOperations.class);
		given(opsForValue.get(key)).willReturn("");
		given(this.stringRedisTemplate.opsForValue()).willReturn(opsForValue);

		ResponseEntity<Map<String, Object>> response = restfulFormLogin(username, DEFAULT_PASSWORD, "123456");
		assertThat(response.getStatusCode()).isSameAs(UNAUTHORIZED);
		assertThat(response.getBody().get("status")).isEqualTo(UNAUTHORIZED.value());
		assertThat(response.getBody().get("message")).isNotEqualTo(UNAUTHORIZED.getReasonPhrase());
		assertThat(response.getBody().get("path")).isEqualTo(DEFAULT_LOGIN_PROCESSING_URL);
	}

	@Test
	void testRestfulFormLoginSuccess() {
		String username = ADMIN_USERNAME;
		String key = DefaultVerificationService.CACHE_NAMESPACE + username;
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> opsForValue = mock(ValueOperations.class);
		given(this.stringRedisTemplate.opsForValue()).willReturn(opsForValue);

		VerificationCodeRequirement requirement = this.testRestTemplate.getForObject("/verificationCode/" + username,
				VerificationCodeRequirement.class);
		assertThat(requirement.isRequired()).isTrue();
		assertThat(requirement.getSendingRequired()).isEqualTo(Boolean.TRUE);

		this.testRestTemplate.postForEntity("/verificationCode/" + username, null, String.class);
		then(opsForValue).should().set(eq(key), this.verificationCodeCaptor.capture(), any(Duration.class));

		String verificationCode = this.verificationCodeCaptor.getValue();

		given(opsForValue.get(key)).willReturn(verificationCode);

		ResponseEntity<Map<String, Object>> response = restfulFormLogin(username, DEFAULT_PASSWORD, verificationCode);
		assertThat(response.getStatusCode()).isSameAs(OK);
		assertThat(response.getBody().get("targetUrl")).isEqualTo(DEFAULT_SUCCESS_URL);
	}

	private ResponseEntity<Map<String, Object>> restfulFormLogin(String username, String password,
			String verificationCode) {
		Map<String, String> data = new LinkedHashMap<>();
		data.put("username", username);
		if (password != null) {
			data.put("password", password);
		}
		if (verificationCode != null) {
			data.put("verificationCode", verificationCode);
		}
		return this.testRestTemplate.exchange(RequestEntity.method(POST, DEFAULT_LOGIN_PROCESSING_URL).body(data),
				new ParameterizedTypeReference<>() {
				});
	}

	private ResponseEntity<String> formLogin(String username, String password, String verificationCode) {
		MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
		data.add("username", username);
		if (password != null) {
			data.add("password", password);
		}
		if (verificationCode != null) {
			data.add("verificationCode", verificationCode);
		}
		return this.testRestTemplate.exchange(RequestEntity.method(POST, DEFAULT_LOGIN_PROCESSING_URL)
			.header(ACCEPT, TEXT_HTML_VALUE)
			.header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
			.body(data), String.class);
	}

	@ComponentScan
	@Configuration
	static class Config {

		@Bean
		PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}

		@Bean
		UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
			return new SimpleInMemoryUserDetailsManager(
					createUser(USER_USERNAME, passwordEncoder.encode(DEFAULT_PASSWORD)),
					createUser(ADMIN_USERNAME, passwordEncoder.encode(DEFAULT_PASSWORD), ADMIN_ROLE));
		}

		private static User createUser(String username, String password, String... roles) {
			return new User(username, password, Stream.of(roles).map(SimpleGrantedAuthority::new).toList());
		}

		@Bean
		VerificationCodeNotifier verificationCodeNotifier() {
			return (receiver, code) -> {
			};
		}

	}

	static class SimpleInMemoryUserDetailsManager extends InMemoryUserDetailsManager {

		SimpleInMemoryUserDetailsManager(UserDetails... users) {
			super(users);
		}

		@Override
		public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
			UserDetails user = super.loadUserByUsername(username);
			if (user == null) {
				throw new UsernameNotFoundException(username);
			}
			return new SimpleUser(user.getUsername(), user.getPassword(), user.isEnabled(), user.isAccountNonExpired(),
					user.isCredentialsNonExpired(), user.isAccountNonLocked(), user.getAuthorities());
		}

	}

	static class SimpleUser extends User implements VerificationAware {

		SimpleUser(String username, String password, boolean enabled, boolean accountNonExpired,
				boolean credentialsNonExpired, boolean accountNonLocked,
				Collection<? extends GrantedAuthority> authorities) {
			super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		}

		@Override
		public String getReceiver() {
			return getUsername();
		}

		@Override
		public boolean isPasswordRequired() {
			return getUsername().equals(ADMIN_USERNAME);
		}

	}

}
