
package io.cornerstone.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
@Retryable(retryFor = { OptimisticLockingFailureException.class, DataIntegrityViolationException.class },
		notRecoverable = OptimisticLockingFailureException.class, noRetryFor = DataIntegrityViolationException.class)
@Transactional
public @interface Idempotent {

	@AliasFor(annotation = Retryable.class)
	String recover();

}
