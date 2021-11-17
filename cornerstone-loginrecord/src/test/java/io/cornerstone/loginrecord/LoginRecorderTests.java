package io.cornerstone.loginrecord;

import java.util.List;

import io.cornerstone.test.SpringApplicationTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = LoginRecorderTests.Config.class)
class LoginRecorderTests extends SpringApplicationTestBase {

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private LoginRecordRepository loginRecordRepository;

	@AfterEach
	void clear() {
		this.loginRecordRepository.deleteAll();
	}

	@Test
	void recordAuthenticationSuccessEvent() {
		this.applicationEventPublisher.publishEvent(new AuthenticationSuccessEvent(
				new UsernamePasswordAuthenticationToken(USER_USERNAME, DEFAULT_PASSWORD)));
		List<LoginRecord> list = this.loginRecordRepository.findAll();
		assertThat(list).hasSize(1);
		LoginRecord entity = list.get(0);
		assertThat(entity.getUsername()).isEqualTo(USER_USERNAME);
		assertThat(entity.getFailed()).isEqualTo(Boolean.FALSE);
		assertThat(entity.getDate()).isNotNull();
	}

	@Test
	void recordAuthenticationFailureBadCredentialsEvent() {
		AuthenticationException ex = new BadCredentialsException("Bad Credentials");
		this.applicationEventPublisher.publishEvent(new AuthenticationFailureBadCredentialsEvent(
				new UsernamePasswordAuthenticationToken(USER_USERNAME, DEFAULT_PASSWORD), ex));
		List<LoginRecord> list = this.loginRecordRepository.findAll();
		assertThat(list).hasSize(1);
		LoginRecord entity = list.get(0);
		assertThat(entity.getUsername()).isEqualTo(USER_USERNAME);
		assertThat(entity.getFailed()).isEqualTo(Boolean.TRUE);
		assertThat(entity.getCause()).isEqualTo(ex.getLocalizedMessage());
		assertThat(entity.getDate()).isNotNull();
	}

	static class Config {

	}

}
