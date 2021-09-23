package io.cornerstone.core.security.verification;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

public interface VerificationAware extends UserDetails {

	@JsonIgnore
	default boolean isVerificationRequired() {
		return true;
	}

	@JsonIgnore
	default boolean isPasswordRequired() {
		return StringUtils.hasLength(getPassword());
	}

	@JsonIgnore
	String getReceiver();

}
