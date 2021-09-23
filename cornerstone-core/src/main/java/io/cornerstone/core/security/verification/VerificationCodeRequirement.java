package io.cornerstone.core.security.verification;

import lombok.Value;

@Value
public class VerificationCodeRequirement {

	private boolean required;

	private Integer length;

	private Boolean passwordHidden;

	private Boolean sendingRequired;

	private Integer resendInterval;

}
