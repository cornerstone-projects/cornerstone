package io.cornerstone.core.metrics;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.cornerstone.core.Application;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration(proxyBeanMethods = false)
public class MetricsConfiguration {

	@Bean
	MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(Application application) {
		return registry -> registry.config().commonTags("app", application.getName(), "instance",
				application.getInstanceId());
	}

	@Bean
	TimedAspect timedAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}

}
