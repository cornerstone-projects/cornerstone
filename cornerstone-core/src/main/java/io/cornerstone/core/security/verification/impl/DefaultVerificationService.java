package io.cornerstone.core.security.verification.impl;

import java.util.Arrays;

import io.cornerstone.core.security.verification.VerificationCodeGenerator;
import io.cornerstone.core.security.verification.VerificationCodeNotifier;
import io.cornerstone.core.security.verification.VerificationProperties;
import io.cornerstone.core.security.verification.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@Slf4j
@RequiredArgsConstructor
public class DefaultVerificationService implements VerificationService {

	public static final String CACHE_NAMESPACE = "verification:";

	private static final String SUFFIX_RESEND = "$$resend";

	private static final String SUFFIX_THRESHOLD = "$$threshold";

	private final VerificationCodeGenerator verficationCodeGenerator;

	private final VerificationCodeNotifier verificationCodeNotifier;

	private final VerificationProperties properties;

	private final StringRedisTemplate stringRedisTemplate;

	@Override
	public void send(String receiver, final String verficationCode) {
		ValueOperations<String, String> opsForValue = this.stringRedisTemplate.opsForValue();
		String codeToSend;
		if (verficationCode == null) {
			codeToSend = opsForValue.get(CACHE_NAMESPACE + receiver);
			if (codeToSend != null && opsForValue.get(CACHE_NAMESPACE + receiver + SUFFIX_RESEND) != null) {
				log.warn("{} is trying resend within cooldown time", receiver);
				return;
			}
		}
		else {
			codeToSend = verficationCode;
			opsForValue.set(CACHE_NAMESPACE + receiver, codeToSend, this.properties.getExpiry());
		}
		if (codeToSend == null || !this.properties.isReuse()) {
			codeToSend = this.verficationCodeGenerator.generator(receiver, this.properties.getLength());
			opsForValue.set(CACHE_NAMESPACE + receiver, codeToSend, this.properties.getExpiry());
		}
		this.verificationCodeNotifier.send(receiver, codeToSend);
		opsForValue.set(CACHE_NAMESPACE + receiver + SUFFIX_RESEND, "", this.properties.getResend().getInterval());
	}

	@Override
	public boolean verify(String receiver, String verificationCode) {
		ValueOperations<String, String> opsForValue = this.stringRedisTemplate.opsForValue();
		boolean verified = verificationCode != null
				&& verificationCode.equals(opsForValue.get(CACHE_NAMESPACE + receiver));
		if (!verified) {
			String key = CACHE_NAMESPACE + receiver + SUFFIX_THRESHOLD;
			Long times = opsForValue.increment(key);
			this.stringRedisTemplate.expire(key, this.properties.getExpiry());
			if (times != null && times >= this.properties.getVerify().getMaxAttempts()) {
				this.stringRedisTemplate.delete(
						Arrays.asList(CACHE_NAMESPACE + receiver, CACHE_NAMESPACE + receiver + SUFFIX_THRESHOLD));
			}
		}
		else {
			this.stringRedisTemplate
					.delete(Arrays.asList(CACHE_NAMESPACE + receiver, CACHE_NAMESPACE + receiver + SUFFIX_THRESHOLD));
		}
		return verified;
	}

}
