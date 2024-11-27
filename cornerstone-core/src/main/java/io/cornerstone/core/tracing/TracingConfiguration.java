package io.cornerstone.core.tracing;

import java.time.Duration;

import io.cornerstone.core.Application;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.exporter.SpanExportingPredicate;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.boot.actuate.autoconfigure.tracing.SdkTracerProviderBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnEnabledTracing
public class TracingConfiguration {

	@Autowired
	private Application application;

	@Bean
	SdkTracerProviderBuilderCustomizer resourceSdkTracerProviderBuilderCustomizer() {
		return builder -> {
			AttributesBuilder attributesBuilder = Attributes.builder();
			attributesBuilder.put("service.name", this.application.getName());
			attributesBuilder.put("service.instance.id", this.application.getInstanceId());
			attributesBuilder.put("server.info", this.application.getServerInfo());
			attributesBuilder.put("java.version", System.getProperty("java.version"));
			builder.setResource(Resource.getDefault().merge(Resource.create(attributesBuilder.build())));
		};
	}

	@Bean
	SpanExportingPredicate noTinySpanExportingPredicate(
			@Value("${management.tracing.span.minimum-duration:PT0.001S}") Duration minimumDuration) {
		return span -> {
			Duration duration = Duration.between(span.getStartTimestamp(), span.getEndTimestamp());
			return duration.compareTo(minimumDuration) > 0;
		};
	}

	@Bean
	@ConditionalOnClass(MicrometerTracing.class)
	ClientResourcesBuilderCustomizer clientResourcesBuilderCustomizerForTracing(ObservationRegistry observationRegistry,
			Environment env) {
		return builder -> builder
			.tracing(new MicrometerTracing(observationRegistry, "redis", env.matchesProfiles("dev")));
	}

}
