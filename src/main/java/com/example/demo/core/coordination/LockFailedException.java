package com.example.demo.core.coordination;

public class LockFailedException extends RuntimeException {

	private static final long serialVersionUID = -8409374328107170094L;

	public LockFailedException(String principal) {
		super(principal);
	}

	public LockFailedException(Exception cause) {
		super(cause);
	}

}