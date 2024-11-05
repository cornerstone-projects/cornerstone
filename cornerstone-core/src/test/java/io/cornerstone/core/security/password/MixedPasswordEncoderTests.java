package io.cornerstone.core.security.password;

import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class MixedPasswordEncoderTests {

	private final PasswordEncoder passwordEncoder = new MixedPasswordEncoder();

	@Test
	void testEncodeAndMatches() {
		assertThat(this.passwordEncoder.matches("secret", "1".repeat(48))).isFalse();
		assertThat(this.passwordEncoder.matches("secret", this.passwordEncoder.encode("secret"))).isTrue();
	}

	@Test
	void testFallbackToDelegatingPasswordEncoder() {
		assertThat(this.passwordEncoder.matches("secret", "{noop}secret")).isTrue();
		assertThat(this.passwordEncoder.matches("secret",
				"{bcrypt}$2a$10$jdJGhzsiIqYFpjJiYWMl/eKDOd8vdyQis2aynmFN0dgJ53XvpzzwC"))
			.isTrue();
	}

}
