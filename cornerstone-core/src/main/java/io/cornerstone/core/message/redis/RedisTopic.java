package io.cornerstone.core.message.redis;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.SerializationException;

import io.cornerstone.core.Application;
import io.cornerstone.core.domain.Scope;
import io.cornerstone.core.util.ExceptionUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class RedisTopic<T extends Serializable> implements io.cornerstone.core.message.Topic<T> {

	@Setter
	protected String channelName;

	@Autowired
	private RedisTemplate<String, T> redisTemplate;

	@Autowired(required = false)
	@Qualifier("globalRedisTemplate")
	private RedisTemplate<String, T> globalRedisTemplate;

	@Autowired
	private RedisMessageListenerContainer redisMessageListenerContainer;

	@Autowired(required = false)
	@Qualifier("globalRedisMessageListenerContainer")
	private RedisMessageListenerContainer globalRedisMessageListenerContainer;

	@Setter
	@Autowired(required = false)
	private Executor taskExecutor;

	public RedisTopic() {
		Class<?> clazz = ResolvableType.forClass(getClass()).as(RedisTopic.class).resolveGeneric(0);
		if (clazz == null)
			throw new IllegalArgumentException(getClass().getName() + " should be generic");
		channelName = clazz.getName();
	}

	@PostConstruct
	public void afterPropertiesSet() {
		Topic globalTopic = new ChannelTopic(getChannelName(Scope.GLOBAL));
		Topic applicationTopic = new ChannelTopic(getChannelName(Scope.APPLICATION));
		if (globalRedisTemplate != null) {
			doSubscribe(globalRedisMessageListenerContainer, globalRedisTemplate, globalTopic);
			doSubscribe(redisMessageListenerContainer, redisTemplate, applicationTopic);
		} else {
			doSubscribe(redisMessageListenerContainer, redisTemplate, globalTopic, applicationTopic);
		}
	}

	@SuppressWarnings("unchecked")
	private void doSubscribe(RedisMessageListenerContainer container, RedisTemplate<String, T> template,
			Topic... topics) {
		container.addMessageListener((message, pattern) -> {
			try {
				T msg = (T) template.getValueSerializer().deserialize(message.getBody());
				log.info("Receive published message: {}", msg);
				subscribe(msg);
			} catch (SerializationException e) {
				// message from other app
				if (ExceptionUtils.getRootCause(e) instanceof ClassNotFoundException) {
					log.warn(e.getMessage());
				} else {
					throw e;
				}
			}
		}, Arrays.asList(topics));
	}

	private String getChannelName(Scope scope) {
		StringBuilder sb = new StringBuilder(channelName).append(".");
		if (scope == Scope.APPLICATION)
			Application.current().ifPresent(a -> sb.append(a.getName()));
		return sb.toString();
	}

	@Override
	public void publish(T message, Scope scope) {
		log.info("Publishing {} message: {}", scope.name(), message);
		if (scope == Scope.LOCAL) {
			Runnable task = () -> subscribe(message);
			if (taskExecutor != null)
				taskExecutor.execute(task);
			else
				task.run();
		} else {
			if (globalRedisTemplate != null && scope == Scope.GLOBAL)
				globalRedisTemplate.convertAndSend(getChannelName(scope), message);
			else
				redisTemplate.convertAndSend(getChannelName(scope), message);
		}
	}

}
