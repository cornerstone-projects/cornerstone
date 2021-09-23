package io.cornerstone.core.security.verification;

import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

public interface VerificationManager {

	boolean isVerificationRequired(String username);

	default boolean isVerificationRequired(UserDetails user) {
		return (user instanceof VerificationAware) && ((VerificationAware) user).isVerificationRequired();
	}

	boolean isPasswordRequired(String username);

	default boolean isPasswordRequired(UserDetails user) {
		boolean isVerificationAware = user instanceof VerificationAware;
		return !isVerificationAware && StringUtils.hasLength(user.getPassword())
				|| isVerificationAware && ((VerificationAware) user).isPasswordRequired();
	}

	@Nullable
	default String getReceiver(UserDetails user) {
		String receiver = null;
		if (user instanceof VerificationAware) {
			receiver = ((VerificationAware) user).getReceiver();
		}
		return receiver;
	}

	void send(String username);

	boolean verify(UserDetails user, String verificationCode);

}
