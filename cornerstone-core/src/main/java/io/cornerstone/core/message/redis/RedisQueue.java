package io.cornerstone.core.message.redis;

import java.io.Serializable;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import io.cornerstone.core.message.Queue;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.support.collections.DefaultRedisList;

@Slf4j
public abstract class RedisQueue<T extends Serializable> implements Queue<T> {

	@Autowired
	@Setter
	private RedisConnectionFactory redisConnectionFactory;

	@Setter
	private String queueName;

	@Setter
	private boolean consuming;

	private final AtomicBoolean stopConsuming = new AtomicBoolean();

	private Thread worker;

	protected BlockingDeque<T> queue;

	public RedisQueue() {
		Class<?> clazz = GenericTypeResolver.resolveTypeArgument(getClass(), RedisQueue.class);
		if (clazz == null) {
			throw new IllegalArgumentException(getClass().getName() + " should be generic");
		}
		this.queueName = clazz.getName();
	}

	@PostConstruct
	public void afterPropertiesSet() {
		RedisTemplate<String, T> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(this.redisConnectionFactory);
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.afterPropertiesSet();
		this.queue = new DefaultRedisList<>(this.queueName, redisTemplate);
		if (this.consuming) {
			Runnable task = () -> {
				while (!this.stopConsuming.get()) {
					try {
						consume(this.queue.take());
					}
					catch (Throwable ex) {
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
