package io.cornerstone.core.session;

import java.util.List;
import java.util.stream.Stream;

import io.cornerstone.core.redis.serializer.CompactJdkSerializationRedisSerializer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisSessionRepository.class)
@ConditionalOnBean(RedisConnectionFactory.class)
@EnableSpringHttpSession
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

	static class CompositeHttpSessionIdResolver implements HttpSessionIdResolver {

		private final HttpSessionIdResolver[] resolvers;

		CompositeHttpSessionIdResolver(HttpSessionIdResolver... resolvers) {
			this.resolvers = resolvers;
		}

		@Override
		public List<String> resolveSessionIds(HttpServletRequest request) {
			return Stream.of(this.resolvers)
				.flatMap((resolver) -> resolver.resolveSessionIds(request).stream())
				.distinct()
				.toList();
		}

		@Override
		public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
			for (HttpSessionIdResolver resolver : this.resolvers) {
				resolver.setSessionId(request, response, sessionId);
			}
		}

		@Override
		public void expireSession(HttpServletRequest request, HttpServletResponse response) {
			for (HttpSessionIdResolver resolver : this.resolvers) {
				resolver.expireSession(request, response);
			}
		}

	}

}
