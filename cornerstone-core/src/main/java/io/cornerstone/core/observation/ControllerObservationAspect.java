package io.cornerstone.core.observation;

import io.cornerstone.core.web.BaseRestController;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.web.bind.annotation.RequestMapping;

@Aspect
public class ControllerObservationAspect extends AbstractObservationAspect {

	public ControllerObservationAspect(ObservationRegistry registry) {
		super(registry);
	}

	@Around("@annotation(requestMapping)")
	public Object observe(ProceedingJoinPoint pjp, RequestMapping requestMapping) throws Throwable {
		return super.observe(pjp);
	}

	@Around("target(controller)")
	public Object observe(ProceedingJoinPoint pjp, BaseRestController controller) throws Throwable {
		return super.observe(pjp);
	}

}
