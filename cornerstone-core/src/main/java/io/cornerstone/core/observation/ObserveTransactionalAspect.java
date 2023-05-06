package io.cornerstone.core.observation;

import java.lang.reflect.Method;
import java.util.concurrent.CompletionStage;

import io.cornerstone.core.util.ReflectionUtils;
import io.micrometer.common.lang.Nullable;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.Transactional;

@Aspect
public class ObserveTransactionalAspect implements Ordered {

	private final ObservationRegistry registry;

	public ObserveTransactionalAspect(ObservationRegistry registry) {
		this.registry = registry;
	}

	@Around("@within(transactional) and not @annotation(org.springframework.transaction.annotation.Transactional)")
	public Object observeClass(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
		return observe(pjp, transactional);
	}

	@Around("execution(public * *(..)) and @annotation(transactional)")
	public Object observeMethod(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
		return observe(pjp, transactional);
	}

	private Object observe(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		String name = pjp.getStaticPart().getSignature().getName();
		String contextualName = ReflectionUtils.stringify(method);
		Observation observation = Observation
			.createNotStarted(name, () -> new ObserveTransactionalAspect.ObservedAspectContext(pjp), this.registry)
			.contextualName(contextualName)
			.lowCardinalityKeyValue("readOnly", String.valueOf(transactional.readOnly()));
		if (CompletionStage.class.isAssignableFrom(method.getReturnType())) {
			observation.start();
			Observation.Scope scope = observation.openScope();
			try {
				return ((CompletionStage<?>) pjp.proceed())
					.whenComplete((result, error) -> stopObservation(observation, scope, error));
			}
			catch (Throwable error) {
				stopObservation(observation, scope, error);
				throw error;
			}
			finally {
				scope.close();
			}
		}
		else {
			return observation.observeChecked(() -> pjp.proceed());
		}
	}

	private void stopObservation(Observation observation, Observation.Scope scope, @Nullable Throwable error) {
		if (error != null) {
			observation.error(error);
		}
		scope.close();
		observation.stop();
	}

	public static class ObservedAspectContext extends Observation.Context {

		private final ProceedingJoinPoint proceedingJoinPoint;

		public ObservedAspectContext(ProceedingJoinPoint proceedingJoinPoint) {
			this.proceedingJoinPoint = proceedingJoinPoint;
		}

		public ProceedingJoinPoint getProceedingJoinPoint() {
			return this.proceedingJoinPoint;
		}

	}

	@Override
	public int getOrder() {
		return -1;
	}

}
