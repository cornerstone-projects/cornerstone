package io.cornerstone.core.security.verification;

import lombok.Value;

@Value
public class VerificationCodeRequirement {

	boolean required;

	Integer length;

	Boolean passwordHidden;

	Boolean sendingRequired;

	Integer resendInterval;

}
