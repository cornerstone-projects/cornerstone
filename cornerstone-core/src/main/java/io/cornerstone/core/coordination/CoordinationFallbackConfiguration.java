package io.cornerstone.core.coordination;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.cornerstone.core.Application;
import io.cornerstone.core.coordination.impl.StandaloneLockService;
import io.cornerstone.core.coordination.impl.StandaloneMembership;

@Configuration(proxyBeanMethods = false)
@Profile("test")
public class CoordinationFallbackConfiguration {

	@Bean
	LockService lockService() {
		return new StandaloneLockService();
	}

	@Bean
	Membership membership(Application application) {
		return new StandaloneMembership(application);
	}

}
