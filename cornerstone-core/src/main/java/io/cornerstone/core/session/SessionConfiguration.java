package io.cornerstone.core.session;

import java.time.Duration;

import io.cornerstone.core.redis.serializer.CompactJdkSerializationRedisSerializer;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.session.RedisSessionProperties;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.data.redis.RedisSessionRepository;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisSessionRepository.class)
@ConditionalOnBean(RedisConnectionFactory.class)
@EnableConfigurationProperties({ SessionProperties.class, RedisSessionProperties.class })
@EnableSpringHttpSession
public class SessionConfiguration {

	@Bean
	public RedisSessionRepository sessionRepository(SessionProperties sessionProperties,
			RedisSessionProperties redisSessionProperties, RedisConnectionFactory redisConnectionFactory,
			RedisSerializer<?> springSessionDefaultRedisSerializer,
			ObjectProvider<SessionRepositoryCustomizer<RedisSessionRepository>> sessionRepositoryCustomizers) {

		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setHashKeySerializer(RedisSerializer.string());
		redisTemplate.setValueSerializer(springSessionDefaultRedisSerializer);
		redisTemplate.setHashValueSerializer(springSessionDefaultRedisSerializer);
		redisTemplate.afterPropertiesSet();

		RedisSessionRepository sessionRepository = new RedisSessionRepository(redisTemplate);
		Duration timeout = sessionProperties.getTimeout();
		if (timeout != null) {
			sessionRepository.setDefaultMaxInactiveInterval(timeout);
		}
		sessionRepository.setRedisKeyNamespace(redisSessionProperties.getNamespace());
		sessionRepository.setFlushMode(redisSessionProperties.getFlushMode());
		sessionRepository.setSaveMode(redisSessionProperties.getSaveMode());
		sessionRepositoryCustomizers.forEach(customizer -> customizer.customize(sessionRepository));
		return sessionRepository;
	}

	@Bean
	public RedisSerializer<?> springSessionDefaultRedisSerializer() {
		return new CompactJdkSerializationRedisSerializer(getClass().getClassLoader()) {
			@Override
			public Object deserialize(byte[] bytes) throws SerializationException {
				try {
					return super.deserialize(bytes);
				}
				catch (SerializationException ex) {
					if (ex.getCause() instanceof SerializationFailedException) {
						return null; // ignore SerializationFailedException
					}
					throw ex;
				}
			}
		};
	}

}
