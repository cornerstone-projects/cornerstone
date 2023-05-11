package io.cornerstone.core.observation;

import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.data.repository.Repository;

@Aspect
public class RepositoryObservationAspect extends AbstractObservationAspect {

	public RepositoryObservationAspect(ObservationRegistry registry) {
		super(registry);
	}

	@Around("target(repository) and not @annotation(org.springframework.transaction.annotation.Transactional)")
	public Object observe(ProceedingJoinPoint pjp, Repository<?, ?> repository) throws Throwable {
		return super.observe(pjp);
	}

}
