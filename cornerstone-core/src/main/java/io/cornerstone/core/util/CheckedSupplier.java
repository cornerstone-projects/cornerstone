package io.cornerstone.core.util;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface CheckedSupplier<T, E extends Throwable> {

	T get() throws E;

	default Supplier<T> uncheck() {
		return () -> {
			try {
				return get();
			}
			catch (Throwable ex) {
				return ExceptionUtils.sneakyThrow(ex);
			}
		};
	}

	static <T, E extends Throwable> Supplier<T> unchecked(CheckedSupplier<T, E> supplier) {
		return requireNonNull(supplier).uncheck();
	}

}
