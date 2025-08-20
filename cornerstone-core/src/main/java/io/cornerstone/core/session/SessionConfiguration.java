package io.cornerstone.core.session;

import io.cornerstone.core.redis.serializer.CompactJdkSerializationRedisSerializer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.session.web.http.CompositeHttpSessionIdResolver;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisSessionRepository.class)
@ConditionalOnBean(RedisConnectionFactory.class)
public class SessionConfiguration {

	@ConditionalOnProperty(prefix = "spring.session.redis", name = "repository-type", havingValue = "default",
			matchIfMissing = true)
	@Bean
	SessionRepositoryCustomizer<RedisSessionRepository> serializerSessionRepositoryCustomizer(
			RedisSerializer<?> springSessionDefaultRedisSerializer) {
		return sessionRepository -> {
			if (sessionRepository.getSessionRedisOperations() instanceof RedisTemplate<?, ?> redisTemplate) {
				redisTemplate.setKeySerializer(RedisSerializer.string());
				redisTemplate.setHashKeySerializer(RedisSerializer.string());
				redisTemplate.setValueSerializer(springSessionDefaultRedisSerializer);
				redisTemplate.setHashValueSerializer(springSessionDefaultRedisSerializer);
			}
		};
	}

	@ConditionalOnProperty(prefix = "spring.session.redis", name = "repository-type", havingValue = "indexed")
	@Bean
	SessionRepositoryCustomizer<RedisIndexedSessionRepository> IndexedSerializerSessionRepositoryCustomizer(
			RedisSerializer<?> springSessionDefaultRedisSerializer) {
		return sessionRepository -> {
			if (sessionRepository.getSessionRedisOperations() instanceof RedisTemplate<?, ?> redisTemplate) {
				redisTemplate.setKeySerializer(RedisSerializer.string());
				redisTemplate.setHashKeySerializer(RedisSerializer.string());
				redisTemplate.setValueSerializer(springSessionDefaultRedisSerializer);
				redisTemplate.setHashValueSerializer(springSessionDefaultRedisSerializer);
			}
		};
	}

	@Bean
	RedisSerializer<?> springSessionDefaultRedisSerializer() {
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

	@Bean
	HttpSessionIdResolver httpSessionIdResolver() {
		return new CompositeHttpSessionIdResolver(HeaderHttpSessionIdResolver.xAuthToken(),
				new CookieHttpSessionIdResolver());
	}

}
