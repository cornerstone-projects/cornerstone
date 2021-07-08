package com.example.demo.core.coordination.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.demo.core.coordination.LockService;

public class StandaloneLockService implements LockService {

	private Map<String, Long> locks = new ConcurrentHashMap<>();

	@Override
	public boolean tryLock(String name) {
		return locks.putIfAbsent(name, Thread.currentThread().getId()) == null;
	}

	@Override
	public void unlock(String name) {
		if (!locks.remove(name, Thread.currentThread().getId())) {
			throw new IllegalStateException(
					"Lock[" + name + "] is not held by thread:" + Thread.currentThread().getName());
		}
	}

}