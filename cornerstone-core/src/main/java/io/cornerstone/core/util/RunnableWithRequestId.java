package io.cornerstone.core.util;

public class RunnableWithRequestId implements Runnable {

	private final Runnable delegate;

	public RunnableWithRequestId(Runnable delegate) {
		this.delegate = delegate;
	}

	@Override
	public void run() {
		boolean requestIdGenerated = CodecUtils.putRequestIdIfAbsent();
		try {
			this.delegate.run();
		}
		finally {
			if (requestIdGenerated) {
				CodecUtils.removeRequestId();
			}
		}
	}

}
