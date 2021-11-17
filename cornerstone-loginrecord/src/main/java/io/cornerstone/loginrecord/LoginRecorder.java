
package io.cornerstone.loginrecord;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

import lombok.RequiredArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@RequiredArgsConstructor
public class LoginRecorder {

	private final LoginRecordRepository loginRecordRepository;

	@EventListener
	private void listen(AuthenticationSuccessEvent event) {
		record(event);
	}

	@EventListener
	private void listen(AuthenticationFailureBadCredentialsEvent event) {
		record(event);
	}

	public void record(AbstractAuthenticationEvent event) {
		LoginRecord entity = new LoginRecord();
		Authentication auth = event.getAuthentication();
		entity.setUsername(auth.getName());
		entity.setDate(
				LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimestamp()), TimeZone.getDefault().toZoneId()));
		Object details = auth.getDetails();
		if (details instanceof WebAuthenticationDetails) {
			WebAuthenticationDetails wad = (WebAuthenticationDetails) details;
			entity.setAddress(wad.getRemoteAddress());
			entity.setSessionId(wad.getSessionId());
		}
		if (event instanceof AbstractAuthenticationFailureEvent) {
			entity.setFailed(Boolean.TRUE);
			entity.setCause(((AbstractAuthenticationFailureEvent) event).getException().getLocalizedMessage());
		}
		else {
			entity.setFailed(Boolean.FALSE);
		}
		this.loginRecordRepository.save(entity);
	}

}
