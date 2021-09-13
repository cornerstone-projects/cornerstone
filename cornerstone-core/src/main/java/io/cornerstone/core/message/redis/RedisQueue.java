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
		if (clazz == null) {
			throw new IllegalArgumentException(getClass().getName() + " should be generic");
		}
		this.queueName = clazz.getName();
	}

	@PostConstruct
	public void afterPropertiesSet() {
		this.queue = new DefaultRedisList<>(this.queueName, this.redisTemplate);
		if (this.consuming) {
			Runnable task = () -> {
				while (!this.stopConsuming.get()) {
					try {
						consume(this.queue.take());
					} catch (Throwable ex) {
						if (Thread.interrupted()) {
							break;
						}
						log.error(ex.getMessage(), ex);
					}
				}
			};
			this.worker = new Thread(task, "redis-queue-consumer-" + getClass().getSimpleName());
			this.worker.setDaemon(true);
			this.worker.start();
		}
	}

	@PreDestroy
	public void stop() {
		if (this.stopConsuming.compareAndSet(false, true)) {
			if (this.worker != null) {
				this.worker.interrupt();
			}
		}
	}

	@Override
	public void produce(T message) {
		this.queue.add(message);
	}

}
