package io.cornerstone.core.security.verification.impl;

import io.cornerstone.core.security.VerificationCodeChecker;
import io.cornerstone.core.security.verification.VerificationManager;
import io.cornerstone.core.security.verification.WrongVerificationCodeException;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;

@Order(0)
@RequiredArgsConstructor
public class DefaultVerificationCodeChecker implements VerificationCodeChecker, MessageSourceAware {

	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	private final VerificationManager verificationManager;

	private final boolean verificationCodeQualified;

	@Override
	public boolean skip(String username) {
		return !this.verificationManager.isVerificationRequired(username);
	}

	@Override
	public boolean skip(UserDetails userDetails) {
		return !this.verificationManager.isVerificationRequired(userDetails);
	}

	@Override
	public boolean skipSend() {
		return false;
	}

	@Override
	public void verify(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication,
			String verificationCode) {
		if (verificationCode == null && skipPasswordCheck(userDetails)) {
			// use password parameter instead for exchange access token
			verificationCode = String.valueOf(authentication.getCredentials());
		}
		if (!this.verificationManager.verify(userDetails, verificationCode)) {
			throw new WrongVerificationCodeException(this.messages.getMessage("wrong.verification.code"));
		}
	}

	@Override
	public boolean skipPasswordCheck(UserDetails userDetails) {
		return this.verificationCodeQualified && this.verificationManager.isVerificationRequired(userDetails)
				&& !this.verificationManager.isPasswordRequired(userDetails);
	}

	@Override
	public boolean skipPasswordCheck(String username) {
		return this.verificationCodeQualified && this.verificationManager.isVerificationRequired(username)
				&& !this.verificationManager.isPasswordRequired(username);
	}

	@Override
	public void setMessageSource(MessageSource messageSource) {
		this.messages = new MessageSourceAccessor(messageSource);
	}

}
