package io.cornerstone.core.util;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

@FunctionalInterface
public interface CheckedConsumer<T, E extends Throwable> {

	void accept(T t) throws E;

	default Consumer<T> uncheck() {
		return (t) -> {
			try {
				accept(t);
			} catch (Throwable ex) {
				ExceptionUtils.sneakyThrow(ex);
			}
		};
	}

	static <T, E extends Throwable> Consumer<T> unchecked(CheckedConsumer<T, E> consumer) {
		return requireNonNull(consumer).uncheck();
	}

}
