package io.cornerstone.core.tracing;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;

@FunctionalInterface
public interface SdkTracerProviderBuilderCustomizer {

	void customize(SdkTracerProviderBuilder builder);

	default void customize(AttributesBuilder builder) {
	}

}
