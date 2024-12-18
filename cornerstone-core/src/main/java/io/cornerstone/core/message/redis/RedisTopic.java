package io.cornerstone.core.message.redis;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Executor;

import io.cornerstone.core.Application;
import io.cornerstone.core.domain.Scope;
import io.cornerstone.core.util.ExceptionUtils;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.SerializationException;

@Slf4j
public abstract class RedisTopic<T extends Serializable> implements io.cornerstone.core.message.Topic<T> {

	@Setter
	protected String channelName;

	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;

	@Autowired(required = false)
	@Qualifier("globalRedisTemplate")
	private RedisTemplate<Object, Object> globalRedisTemplate;

	@Autowired
	private RedisMessageListenerContainer redisMessageListenerContainer;

	@Autowired(required = false)
	@Qualifier("globalRedisMessageListenerContainer")
	private RedisMessageListenerContainer globalRedisMessageListenerContainer;

	@Setter
	@Autowired(required = false)
	private Executor taskExecutor;

	public RedisTopic() {
		Class<?> clazz = GenericTypeResolver.resolveTypeArgument(getClass(), RedisTopic.class);
		if (clazz == null) {
			throw new IllegalArgumentException(getClass().getName() + " should be generic");
		}
		this.channelName = clazz.getName();
	}

	@PostConstruct
	public void afterPropertiesSet() {
		Topic globalTopic = new ChannelTopic(getChannelName(Scope.GLOBAL));
		Topic applicationTopic = new ChannelTopic(getChannelName(Scope.APPLICATION));
		if (this.globalRedisTemplate != null) {
			doSubscribe(this.globalRedisMessageListenerContainer, this.globalRedisTemplate, globalTopic);
			doSubscribe(this.redisMessageListenerContainer, this.redisTemplate, applicationTopic);
		}
		else {
			doSubscribe(this.redisMessageListenerContainer, this.redisTemplate, globalTopic, applicationTopic);
		}
	}

	@SuppressWarnings("unchecked")
	private void doSubscribe(RedisMessageListenerContainer container, RedisTemplate<Object, Object> template,
			Topic... topics) {
		container.addMessageListener((message, pattern) -> {
			try {
				T msg = (T) template.getValueSerializer().deserialize(message.getBody());
				log.info("Receive published message: {}", msg);
				subscribe(msg);
			}
			catch (SerializationException ex) {
				// message from other app
				if (ExceptionUtils.getRootCause(ex) instanceof ClassNotFoundException) {
					log.warn(ex.getMessage());
				}
				else {
					throw ex;
				}
			}
		}, List.of(topics));
	}

	private String getChannelName(Scope scope) {
		StringBuilder sb = new StringBuilder(this.channelName).append(".");
		if (scope == Scope.APPLICATION) {
			Application.current().ifPresent(a -> sb.append(a.getName()));
		}
		return sb.toString();
	}

	@Override
	public void publish(T message, Scope scope) {
		log.info("Publishing {} message: {}", scope.name(), message);
		if (scope == Scope.LOCAL) {
			Runnable task = () -> subscribe(message);
			if (this.taskExecutor != null) {
				this.taskExecutor.execute(task);
			}
			else {
				task.run();
			}
		}
		else {
			if ((this.globalRedisTemplate != null) && (scope == Scope.GLOBAL)) {
				this.globalRedisTemplate.convertAndSend(getChannelName(scope), message);
			}
			else {
				this.redisTemplate.convertAndSend(getChannelName(scope), message);
			}
		}
	}

}
