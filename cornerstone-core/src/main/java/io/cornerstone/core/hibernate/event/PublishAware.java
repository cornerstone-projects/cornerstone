package io.cornerstone.core.hibernate.event;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.cornerstone.core.domain.Scope;

@Target(TYPE)
@Retention(RUNTIME)
@Inherited
public @interface PublishAware {

	Scope scope() default Scope.APPLICATION;

}
