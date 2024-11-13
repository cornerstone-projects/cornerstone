package io.cornerstone.core.validation.validators;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SocialCreditIdentifierValidatorTests {

	@Test
	void testIsValid() {
		assertThat(SocialCreditIdentifierValidator.isValid("")).isFalse();
		assertThat(SocialCreditIdentifierValidator.isValid("10000000")).isFalse();
		assertThat(SocialCreditIdentifierValidator.isValid("43022419840628423A")).isFalse();
		assertThat(SocialCreditIdentifierValidator.isValid("43022419840628423X")).isFalse();
		assertThat(SocialCreditIdentifierValidator.isValid("430224198309145163")).isFalse();
		assertThat(SocialCreditIdentifierValidator.isValid("91350100M000100y43")).isFalse();
		assertThat(SocialCreditIdentifierValidator.isValid("91350100M000100Y43")).isTrue();
		assertThat(SocialCreditIdentifierValidator.isValid("92341321MA2REGCG29")).isTrue();
		assertThat(SocialCreditIdentifierValidator.isValid("92220103MA152C47XT")).isTrue();
		assertThat(SocialCreditIdentifierValidator.isValid("92340202MA2REG3M4F")).isTrue();
		assertThat(SocialCreditIdentifierValidator.isValid("91510108MA6C8G954F")).isTrue();
		assertThat(SocialCreditIdentifierValidator.isValid("92370725MA3MJ0PK96")).isTrue();
		assertThat(SocialCreditIdentifierValidator.isValid("31110000358343139K")).isTrue();
	}

	@Test
	void testRandomValue() {
		for (int i = 0; i < 100; i++) {
			assertThat(SocialCreditIdentifierValidator.isValid(SocialCreditIdentifierValidator.randomValue())).isTrue();
		}
	}

}
