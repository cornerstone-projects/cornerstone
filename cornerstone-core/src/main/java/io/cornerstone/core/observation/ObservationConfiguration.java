package io.cornerstone.core.observation;

import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservationConfiguration {

	@Bean
	ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
		return new ObservedAspect(observationRegistry);
	}

	@Bean
	ObserveTransactionalAspect observeTransactionalAspect(ObservationRegistry observationRegistry) {
		return new ObserveTransactionalAspect(observationRegistry);
	}

	@Bean
	ObservationPredicate noSpringSecurityObservations() {
		return (name, context) -> !name.startsWith("spring.security.");
	}

}
