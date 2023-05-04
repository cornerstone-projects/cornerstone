package io.cornerstone.core.observation;

import io.cornerstone.core.Application;
import io.micrometer.common.KeyValue;
import io.micrometer.observation.ObservationFilter;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservationConfiguration {

	@Bean
	public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
		return new ObservedAspect(observationRegistry);
	}

	@Bean
	public ObservationFilter globalObservationFilter(Application application) {
		return context -> {
			// both for metrics and spans
			context.addLowCardinalityKeyValue(KeyValue.of("instance.id", application.getInstanceId()));
			// spans-only
			context.addHighCardinalityKeyValue(KeyValue.of("server.info", application.getServerInfo()));
			context.addHighCardinalityKeyValue(KeyValue.of("java.version", System.getProperty("java.version")));
			return context;
		};
	}

}
