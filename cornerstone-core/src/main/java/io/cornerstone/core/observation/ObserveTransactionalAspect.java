package io.cornerstone.core.observation;

import io.micrometer.common.KeyValues;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.transaction.annotation.Transactional;

@Aspect
public class ObserveTransactionalAspect extends AbstractObservationAspect {

	public ObserveTransactionalAspect(ObservationRegistry registry) {
		super(registry);
	}

	@Around("@within(transactional) and not @annotation(org.springframework.transaction.annotation.Transactional)")
	public Object observeClass(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
		return observe(pjp, KeyValues.of("readOnly", String.valueOf(transactional.readOnly())));
	}

	@Around("execution(public * *(..)) and @annotation(transactional)")
	public Object observeMethod(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
		return observe(pjp, KeyValues.of("readOnly", String.valueOf(transactional.readOnly())));
	}

}
