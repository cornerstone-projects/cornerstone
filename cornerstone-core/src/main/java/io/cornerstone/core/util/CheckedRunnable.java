package io.cornerstone.core.util;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface CheckedRunnable<E extends Throwable> {

	void run() throws E;

	default Runnable uncheck() {
		return () -> {
			try {
				run();
			}
			catch (Throwable ex) {
				ExceptionUtils.sneakyThrow(ex);
			}
		};
	}

	static <E extends Throwable> Runnable unchecked(CheckedRunnable<E> runnable) {
		return requireNonNull(runnable).uncheck();
	}

}
