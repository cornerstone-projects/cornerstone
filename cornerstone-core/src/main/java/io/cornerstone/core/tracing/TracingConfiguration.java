package io.cornerstone.core.tracing;

import java.util.Map.Entry;

import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.sdk.trace.SdkTracerProvider;

import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnEnabledTracing
@ConditionalOnClass({ OtelTracer.class, SdkTracerProvider.class, OpenTelemetry.class, OtlpHttpSpanExporter.class })
@EnableConfigurationProperties(OtlpProperties.class)
public class TracingConfiguration {

	@Bean
	@ConditionalOnMissingBean(value = OtlpHttpSpanExporter.class,
			type = "io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter")
	OtlpHttpSpanExporter otlpHttpSpanExporter(OtlpProperties properties) {
		OtlpHttpSpanExporterBuilder builder = OtlpHttpSpanExporter.builder()
			.setEndpoint(properties.getEndpoint())
			.setTimeout(properties.getTimeout())
			.setCompression(properties.getCompression().name().toLowerCase());
		for (Entry<String, String> header : properties.getHeaders().entrySet()) {
			builder.addHeader(header.getKey(), header.getValue());
		}
		return builder.build();
	}

}
