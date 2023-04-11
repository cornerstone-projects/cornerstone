package io.cornerstone.fs;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import io.cornerstone.core.util.ReflectionUtils;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
@RequiredArgsConstructor
public class FileStorageInstrumentation {

	private final ObservationRegistry observationRegistry;

	@Around("execution(* *.*(..)) and target(fileStorage)")
	public Object timing(ProceedingJoinPoint pjp, FileStorage fileStorage) throws Throwable {
		Object[] args = pjp.getArgs();
		Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		String methodName = method.getName();
		if (methodName.startsWith("get") || methodName.startsWith("is")) {
			return pjp.proceed();
		}
		boolean error = false;
		long start = System.nanoTime();
		try {
			Object result = Observation.createNotStarted("fileStorage", this.observationRegistry)
				.contextualName(ReflectionUtils.stringify(method))
				.lowCardinalityKeyValue("name", fileStorage.getName())
				.observeChecked(() -> pjp.proceed());
			if (methodName.equals("write") && (args.length > 2) && (args[2] instanceof Long lo)) {
				Metrics.summary("fs.write.size", "name", fileStorage.getName()).record(lo);
			}
			return result;
		}
		catch (Exception ex) {
			error = true;
			throw ex;
		}
		finally {
			Metrics
				.timer("fs.operations", "name", fileStorage.getName(), "operation", methodName, "error",
						String.valueOf(error))
				.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
		}
	}

}
