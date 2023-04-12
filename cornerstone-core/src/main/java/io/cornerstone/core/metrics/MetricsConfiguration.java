package io.cornerstone.core.metrics;

import io.cornerstone.core.Application;
import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MetricsConfiguration {

	@Bean
	MeterRegistryCustomizer<MeterRegistry> meterRegistryCustomizer(Application application) {
		return registry -> registry.config()
			.commonTags("app", application.getName(), "instance", application.getInstanceId());
	}

	@Bean
	TimedAspect timedAspect(MeterRegistry meterRegistry) {
		return new TimedAspect(meterRegistry);
	}

	@Bean
	CountedAspect countedAspect(MeterRegistry meterRegistry) {
		return new CountedAspect(meterRegistry);
	}

}
