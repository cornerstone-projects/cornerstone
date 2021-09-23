package io.cornerstone.core.security.verification;

import org.springframework.security.core.AuthenticationException;

public class WrongVerificationCodeException extends AuthenticationException {

	private static final long serialVersionUID = 137113247989004952L;

	public WrongVerificationCodeException(String msg) {
		super(msg);
	}

}
