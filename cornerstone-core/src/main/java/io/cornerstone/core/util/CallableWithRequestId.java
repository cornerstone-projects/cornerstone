package io.cornerstone.core.util;

import java.util.concurrent.Callable;

public class CallableWithRequestId<V> implements Callable<V> {

	private final Callable<V> delegate;

	public CallableWithRequestId(Callable<V> delegate) {
		this.delegate = delegate;
	}

	@Override
	public V call() throws Exception {
		boolean requestIdGenerated = CodecUtils.putRequestIdIfAbsent();
		try {
			return this.delegate.call();
		}
		finally {
			if (requestIdGenerated) {
				CodecUtils.removeRequestId();
			}
		}
	}

}
