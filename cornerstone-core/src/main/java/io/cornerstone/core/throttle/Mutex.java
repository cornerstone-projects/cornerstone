package io.cornerstone.core.throttle;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.cornerstone.core.domain.Scope;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD })
@Retention(RUNTIME)
public @interface Mutex {

	String key() default "";

	Scope scope() default Scope.GLOBAL;

}
