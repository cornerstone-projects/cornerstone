package io.cornerstone.core.session;

import io.cornerstone.core.redis.serializer.CompactJdkSerializationRedisSerializer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisIndexedSessionRepository.class)
public class SessionConfiguration {

	@Bean
	public RedisSerializer<?> springSessionDefaultRedisSerializer() {
		return new CompactJdkSerializationRedisSerializer() {
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
