package io.cornerstone.core.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

import org.springframework.util.StringUtils;

@Slf4j
public class NameableThreadFactory implements ThreadFactory {

	private final AtomicInteger threadNumber = new AtomicInteger(1);

	private final ThreadGroup group;

	private final String namePrefix;

	private final UncaughtExceptionHandler uncaughtExceptionHandler;

	public NameableThreadFactory(String poolName) {
		this(poolName, null, null);
	}

	public NameableThreadFactory(String poolName, String threadGroupName) {
		this(poolName, threadGroupName, null);
	}

	public NameableThreadFactory(String poolName, UncaughtExceptionHandler uncaughtExceptionHandler) {
		this(poolName, null, uncaughtExceptionHandler);
	}

	public NameableThreadFactory(String poolName, String threadGroupName,
			UncaughtExceptionHandler uncaughtExceptionHandler) {
		this.group = Thread.currentThread().getThreadGroup();
		StringBuilder sb = new StringBuilder();
		if (StringUtils.hasLength(poolName)) {
			sb.append(poolName);
			sb.append("-");
		}
		if (StringUtils.hasLength(threadGroupName)) {
			sb.append(threadGroupName);
			sb.append("-");
		}
		this.namePrefix = sb.toString();
		this.uncaughtExceptionHandler = uncaughtExceptionHandler != null ? uncaughtExceptionHandler : (t, ex) -> {
			log.error(ex.getMessage(), ex);
		};
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0);
		if (t.isDaemon()) {
			t.setDaemon(false);
		}
		if (t.getPriority() != Thread.NORM_PRIORITY) {
			t.setPriority(Thread.NORM_PRIORITY);
		}
		if (this.uncaughtExceptionHandler != null) {
			t.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
		}
		return t;
	}

}
