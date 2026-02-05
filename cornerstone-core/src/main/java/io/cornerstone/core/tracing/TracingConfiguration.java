package io.cornerstone.core.tracing;

import java.time.Duration;

import io.cornerstone.core.Application;
import io.micrometer.tracing.exporter.SpanExportingPredicate;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.micrometer.tracing.autoconfigure.ConditionalOnEnabledTracingExport;
import org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.SdkTracerProviderBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnEnabledTracingExport
public class TracingConfiguration {

	@Autowired
	private Application application;

	@Bean
	SdkTracerProviderBuilderCustomizer resourceSdkTracerProviderBuilderCustomizer() {
		return builder -> {
			AttributesBuilder attributesBuilder = Attributes.builder();
			attributesBuilder.put("service.name", this.application.getName());
			attributesBuilder.put("service.id", this.application.getInstanceId());
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

}
