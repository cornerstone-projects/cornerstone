package io.cornerstone.core.message.redis;

import java.io.Serializable;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.collections.DefaultRedisList;

import io.cornerstone.core.message.Queue;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class RedisQueue<T extends Serializable> implements Queue<T> {

	@Autowired
	@Setter
	private RedisTemplate<String, T> redisTemplate;

	@Setter
	private String queueName;

	@Setter
	private boolean consuming;

	private AtomicBoolean stopConsuming = new AtomicBoolean();

	private Thread worker;

	protected BlockingDeque<T> queue;

	public RedisQueue() {
		Class<?> clazz = ResolvableType.forClass(getClass()).as(RedisQueue.class).resolveGeneric(0);
		if (clazz == null)
			throw new IllegalArgumentException(getClass().getName() + " should be generic");
		queueName = clazz.getName();
	}

	@PostConstruct
	public void afterPropertiesSet() {
		queue = new DefaultRedisList<>(queueName, redisTemplate);
		if (consuming) {
			Runnable task = () -> {
				while (!stopConsuming.get()) {
					try {
						consume(queue.take());
					} catch (Throwable e) {
						if (Thread.interrupted())
							break;
						log.error(e.getMessage(), e);
					}
				}
			};
			worker = new Thread(task, "redis-queue-consumer-" + getClass().getSimpleName());
			worker.setDaemon(true);
			worker.start();
		}
	}

	@PreDestroy
	public void stop() {
		if (stopConsuming.compareAndSet(false, true)) {
			if (worker != null)
				worker.interrupt();
		}
	}

	@Override
	public void produce(T message) {
		queue.add(message);
	}

}
