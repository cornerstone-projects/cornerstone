package io.cornerstone.core.validation.validators;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MobilePhoneNumberValidatorTests {

	@Test
	void testIsValid() {
		assertThat(MobilePhoneNumberValidator.isValid("10000000")).isFalse();
		assertThat(MobilePhoneNumberValidator.isValid("11811111111")).isFalse();
		assertThat(MobilePhoneNumberValidator.isValid("13811111111")).isTrue();
		assertThat(MobilePhoneNumberValidator.isValid("15800000000")).isTrue();
		assertThat(MobilePhoneNumberValidator.isValid("18900000000")).isTrue();
	}

	@Test
	void testRandomValue() {
		for (int i = 0; i < 100; i++) {
			assertThat(MobilePhoneNumberValidator.isValid(MobilePhoneNumberValidator.randomValue())).isTrue();
		}
	}

}
