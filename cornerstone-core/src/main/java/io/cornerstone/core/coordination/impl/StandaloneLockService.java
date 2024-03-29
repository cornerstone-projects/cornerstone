package io.cornerstone.core.coordination.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.cornerstone.core.coordination.LockService;

public class StandaloneLockService implements LockService {

	private final Map<String, Long> locks = new ConcurrentHashMap<>();

	@Override
	public boolean tryLock(String name) {
		return this.locks.putIfAbsent(name, Thread.currentThread().threadId()) == null;
	}

	@Override
	public void unlock(String name) {
		if (!this.locks.remove(name, Thread.currentThread().threadId())) {
			throw new IllegalStateException(
					"Lock[" + name + "] is not held by thread:" + Thread.currentThread().getName());
		}
	}

}
