package com.example.demo.core.coordination;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.example.demo.core.Application;
import com.example.demo.core.coordination.impl.StandaloneLockService;
import com.example.demo.core.coordination.impl.StandaloneMembership;

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
