package io.cornerstone.core.metrics;

import io.cornerstone.core.Application;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MetricsConfiguration {

	@Bean
	MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(Application application) {
		return registry -> registry.config()
			.commonTags("service.name", application.getName(), "service.id", application.getInstanceId());
	}

}
