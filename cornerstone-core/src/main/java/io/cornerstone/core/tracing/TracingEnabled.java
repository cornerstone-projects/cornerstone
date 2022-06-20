package io.cornerstone.core.tracing;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
@ConditionalOnProperty(name = TracingEnabled.KEY, havingValue = "true", matchIfMissing = true)
public @interface TracingEnabled {

	String KEY = "opentracing.enabled";

}
