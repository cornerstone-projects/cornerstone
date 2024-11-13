package io.cornerstone.core.validation.validators;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationCodeValidatorTests {

	@Test
	void testIsValid() {
		assertThat(OrganizationCodeValidator.isValid("")).isFalse();
		assertThat(OrganizationCodeValidator.isValid("10000000")).isFalse();
		assertThat(OrganizationCodeValidator.isValid("123456789")).isFalse();
		assertThat(OrganizationCodeValidator.isValid("111111111")).isFalse();
		assertThat(OrganizationCodeValidator.isValid("AABBCCDDE")).isFalse();
		assertThat(OrganizationCodeValidator.isValid("183888881")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("183974050")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("183807033")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("344701003")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("352864865")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("329420684")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("329436141")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("329420684")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("320714547")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("M000100Y4")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("MA2REGCG2")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("MA152C47X")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("MA2REG3M4")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("MA6C8G954")).isTrue();
		assertThat(OrganizationCodeValidator.isValid("MA3MJ0PK9")).isTrue();
	}

	@Test
	void testRandomValue() {
		for (int i = 0; i < 100; i++) {
			assertThat(OrganizationCodeValidator.isValid(OrganizationCodeValidator.randomValue())).isTrue();
		}
	}

}
