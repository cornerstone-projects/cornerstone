package io.cornerstone.core.security;

import io.cornerstone.core.security.verification.WrongVerificationCodeException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public interface VerificationCodeChecker {

	void verify(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication, String verificationCode)
			throws WrongVerificationCodeException;

	boolean skip(String username);

	default boolean skip(UserDetails userDetails) {
		return skip(userDetails.getUsername());
	}

	boolean skipSend();

	default boolean skipPasswordCheck(String username) {
		return false;
	}

	default boolean skipPasswordCheck(UserDetails userDetails) {
		return skipPasswordCheck(userDetails.getUsername());
	}

}
