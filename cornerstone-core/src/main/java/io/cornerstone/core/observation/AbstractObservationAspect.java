package io.cornerstone.core.observation;

import java.lang.reflect.Method;
import java.util.concurrent.CompletionStage;

import io.micrometer.common.KeyValues;
import io.micrometer.common.lang.Nullable;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.core.Ordered;

public abstract class AbstractObservationAspect implements Ordered {

	private final ObservationRegistry registry;

	public AbstractObservationAspect(ObservationRegistry registry) {
		this.registry = registry;
	}

	protected Object observe(ProceedingJoinPoint pjp) throws Throwable {
		return observe(pjp, KeyValues.empty());
	}

	protected Object observe(ProceedingJoinPoint pjp, KeyValues lowCardinalityKeyValues) throws Throwable {
		return observe(pjp, lowCardinalityKeyValues, KeyValues.empty());
	}

	protected Object observe(ProceedingJoinPoint pjp, KeyValues lowCardinalityKeyValues,
			KeyValues highCardinalityKeyValues) throws Throwable {
		Signature signature = pjp.getStaticPart().getSignature();
		Method method = ((MethodSignature) signature).getMethod();
		String name = "method.observed";
		String contextualName = signature.getDeclaringType().getSimpleName() + "#" + signature.getName();
		Observation observation = Observation.createNotStarted(name, this.registry)
			.contextualName(contextualName)
			.lowCardinalityKeyValue("class", signature.getDeclaringTypeName())
			.lowCardinalityKeyValue("method", signature.getName())
			.lowCardinalityKeyValues(lowCardinalityKeyValues)
			.highCardinalityKeyValues(highCardinalityKeyValues);
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

	@Override
	public int getOrder() {
		return -1;
	}

}
