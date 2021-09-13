package io.cornerstone.core.util;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface CheckedCallable<T, E extends Throwable> {

	T call() throws E;

	default Callable<T> uncheck() {
		return () -> {
			try {
				return call();
			} catch (Throwable ex) {
				return ExceptionUtils.sneakyThrow(ex);
			}
		};
	}

	static <T, E extends Throwable> Callable<T> unchecked(CheckedCallable<T, E> callable) {
		return requireNonNull(callable).uncheck();
	}

}
