package io.cornerstone.core.security.verification;

import java.time.Duration;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(VerificationProperties.PREFIX)
@Data
public class VerificationProperties {

	public static final String PREFIX = "verification.code";

	private boolean qualified = true;

	private Duration expiry = Duration.ofMinutes(5);

	private boolean reuse = true;

	private int length = 6;

	private Resend resend = new Resend();

	private Verify verify = new Verify();

	@Data
	public static class Verify {

		private Duration interval = Duration.ofSeconds(5);

		private int maxAttempts = 5;

	}

	@Data
	public static class Resend {

		private Duration interval = Duration.ofSeconds(60);

	}

}
