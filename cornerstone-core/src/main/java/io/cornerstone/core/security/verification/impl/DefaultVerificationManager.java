package io.cornerstone.core.security.verification.impl;

import io.cornerstone.core.security.verification.VerificationManager;
import io.cornerstone.core.security.verification.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class DefaultVerificationManager implements VerificationManager {

	private final VerificationService verificationService;

	private final UserDetailsService userDetailsService;

	@Override
	public boolean isVerificationRequired(String username) {
		try {
			return isVerificationRequired(this.userDetailsService.loadUserByUsername(username));
		}
		catch (UsernameNotFoundException ex) {
			return false;
		}
	}

	@Override
	public boolean isPasswordRequired(String username) {
		try {
			return isPasswordRequired(this.userDetailsService.loadUserByUsername(username));
		}
		catch (UsernameNotFoundException ex) {
			return true;
		}
	}

	@Override
	public void send(String username) {
		String receiver = getReceiver(this.userDetailsService.loadUserByUsername(username));
		if (!StringUtils.hasLength(receiver)) {
			log.warn("Send failed because receiver not found for user: {}", username);
		}
		else {
			this.verificationService.send(receiver);
		}
	}

	@Override
	public boolean verify(UserDetails user, String verificationCode) {
		String receiver = getReceiver(user);
		if (!StringUtils.hasLength(receiver)) {
			log.warn("Verify failed because receiver not found for user: {}", user.getUsername());
			return false;
		}
		return this.verificationService.verify(receiver, verificationCode);
	}

}
