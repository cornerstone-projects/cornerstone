package io.cornerstone.core.tracing;

import java.util.Map.Entry;

import io.cornerstone.core.Application;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnEnabledTracing
@ConditionalOnClass({ OtelTracer.class, SdkTracerProvider.class, OpenTelemetry.class, OtlpHttpSpanExporter.class })
@EnableConfigurationProperties(OtlpProperties.class)
public class TracingConfiguration {

	@Bean
	SdkTracerProvider otelSdkTracerProvider(Environment environment, ObjectProvider<SpanProcessor> spanProcessors,
			Sampler sampler, ObjectProvider<SdkTracerProviderBuilderCustomizer> customizers) {
		String applicationName = environment.getProperty("spring.application.name", "application");
		AttributesBuilder attributesBuilder = Attributes.builder();
		attributesBuilder.put(ResourceAttributes.SERVICE_NAME, applicationName);
		customizers.orderedStream().forEach((customizer) -> customizer.customize(attributesBuilder));
		SdkTracerProviderBuilder builder = SdkTracerProvider.builder()
			.setSampler(sampler)
			.setResource(Resource.create(attributesBuilder.build()));
		spanProcessors.orderedStream().forEach(builder::addSpanProcessor);
		customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
		return builder.build();
	}

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

	@Bean
	SdkTracerProviderBuilderCustomizer applicationAttributesCustomizer(Application application) {
		return new SdkTracerProviderBuilderCustomizer() {

			@Override
			public void customize(SdkTracerProviderBuilder builder) {

			}

			@Override
			public void customize(AttributesBuilder builder) {
				builder.putAll(Attributes.of(ResourceAttributes.SERVICE_INSTANCE_ID, application.getInstanceId(),
						AttributeKey.stringKey("server.info"), application.getServerInfo(),
						AttributeKey.stringKey("java.version"), System.getProperty("java.version")));
			}
		};
	}

}
