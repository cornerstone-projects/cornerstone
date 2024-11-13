package io.cornerstone.core.validation.validators;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CitizenIdentificationNumberValidatorTests {

	@Test
	void testIsValid() {
		assertThat(CitizenIdentificationNumberValidator.isValid("")).isFalse();
		assertThat(CitizenIdentificationNumberValidator.isValid("10000000")).isFalse();
		assertThat(CitizenIdentificationNumberValidator.isValid("43022419840628423A")).isFalse();
		assertThat(CitizenIdentificationNumberValidator.isValid("43022419840628423X")).isFalse();
		assertThat(CitizenIdentificationNumberValidator.isValid("430224198309145163")).isFalse();
		assertThat(CitizenIdentificationNumberValidator.isValid("440000200100177718")).isFalse();
		assertThat(CitizenIdentificationNumberValidator.isValid("440000197108227711")).isTrue();
		assertThat(CitizenIdentificationNumberValidator.isValid("440000197302188242")).isTrue();
		assertThat(CitizenIdentificationNumberValidator.isValid("440000197301154323")).isTrue();
		assertThat(CitizenIdentificationNumberValidator.isValid("440000197303223134")).isTrue();
		assertThat(CitizenIdentificationNumberValidator.isValid("440000197509196995")).isTrue();
		assertThat(CitizenIdentificationNumberValidator.isValid("440000197405232066")).isTrue();
		assertThat(CitizenIdentificationNumberValidator.isValid("440000197708142017")).isTrue();
		assertThat(CitizenIdentificationNumberValidator.isValid("440000198406198968")).isTrue();
		assertThat(CitizenIdentificationNumberValidator.isValid("440000198207196919")).isTrue();
		assertThat(CitizenIdentificationNumberValidator.isValid("440000199002119420")).isTrue();
		assertThat(CitizenIdentificationNumberValidator.isValid("44000019810613759X")).isTrue();
	}

	@Test
	void testRandomValue() {
		for (int i = 0; i < 100; i++) {
			assertThat(CitizenIdentificationNumberValidator.isValid(CitizenIdentificationNumberValidator.randomValue()))
				.isTrue();
		}
	}

}
