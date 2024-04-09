package io.cornerstone.core.security.verification;

import io.cornerstone.core.security.verification.impl.DefaultVerificationCodeChecker;
import io.cornerstone.core.security.verification.impl.DefaultVerificationManager;
import io.cornerstone.core.security.verification.impl.DefaultVerificationService;
import io.cornerstone.core.util.CodecUtils;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = VerificationProperties.PREFIX, name = "enabled", havingValue = "true")
@EnableConfigurationProperties(VerificationProperties.class)
public class VerificationConfigration {

	@ConditionalOnMissingBean
	@Bean
	VerificationService defaultVerificationService(VerificationCodeGenerator verficationCodeGenerator,
			VerificationCodeNotifier verificationCodeNotifier, VerificationProperties properties,
			StringRedisTemplate stringRedisTemplate) {
		return new DefaultVerificationService(verficationCodeGenerator, verificationCodeNotifier, properties,
				stringRedisTemplate);
	}

	@ConditionalOnMissingBean
	@Bean
	VerificationManager defaultVerificationManager(VerificationService verificationService,
			UserDetailsService userDetailsService) {
		return new DefaultVerificationManager(verificationService, userDetailsService);
	}

	@ConditionalOnMissingBean
	@Bean
	DefaultVerificationCodeChecker defaultVerificationCodeChecker(VerificationManager verificationManager,
			VerificationProperties properties) {
		return new DefaultVerificationCodeChecker(verificationManager, properties.isQualified());
	}

	@Profile("!dev")
	@ConditionalOnMissingBean
	@Bean
	VerificationCodeGenerator defaultVerificationCodeGenerator() {
		return (receiver, length) -> CodecUtils.randomDigitalString(length);
	}

	@Profile("dev")
	@ConditionalOnMissingBean
	@Bean
	VerificationCodeGenerator simpleVerificationCodeGenerator() {
		return (receiver, length) -> "0".repeat(length);
	}

	@Profile("dev")
	@ConditionalOnMissingBean
	@Bean
	VerificationCodeNotifier simpleVerificationCodeNotifier() {
		return (receiver, code) -> System.out.println("send to " + receiver + ": " + code);
	}

}
