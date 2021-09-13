package io.cornerstone.core.util;

public class MaxAttemptsExceededException extends RuntimeException {

	private static final long serialVersionUID = 7599748876516723063L;

	public MaxAttemptsExceededException(int maxAttempts) {
		this(String.valueOf(maxAttempts));
	}

	private MaxAttemptsExceededException(String maxAttempts) {
		super(maxAttempts);
	}

}
