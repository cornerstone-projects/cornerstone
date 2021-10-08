package io.cornerstone.core.tracing;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Traced {

	boolean withActiveSpanOnly() default true;

	String operationName() default "";

	Tag[] tags() default {};

}
