package io.cornerstone.core.security.password;

import java.util.stream.Stream;

import io.cornerstone.test.ControllerTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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

class MixedPasswordEncoderTests {

	private final PasswordEncoder passwordEncoder = new MixedPasswordEncoder();

	@Test
	void testEncodeAndMatches() {
		assertThat(passwordEncoder.matches("secret", "1".repeat(48))).isFalse();
		assertThat(passwordEncoder.matches("secret", passwordEncoder.encode("secret"))).isTrue();
	}

	@Test
	void testFallbackToDelegatingPasswordEncoder() {
		assertThat(passwordEncoder.matches("secret", "{noop}secret")).isTrue();
		assertThat(passwordEncoder.matches("secret",
				"{bcrypt}$2a$10$jdJGhzsiIqYFpjJiYWMl/eKDOd8vdyQis2aynmFN0dgJ53XvpzzwC"))
			.isTrue();
	}

}
