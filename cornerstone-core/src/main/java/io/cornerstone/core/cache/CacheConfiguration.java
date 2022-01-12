package io.cornerstone.core.cache;

import java.lang.reflect.Field;

import io.cornerstone.core.redis.serializer.CompactJdkSerializationRedisSerializer;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

@Profile("!test")
@EnableCaching(order = Ordered.HIGHEST_PRECEDENCE + 3)
@Configuration(proxyBeanMethods = false)
public class CacheConfiguration extends CachingConfigurerSupport {

	@Override
	public CacheErrorHandler errorHandler() {
		return new DefaultCacheErrorHandler();
	}

	@Bean
	@ConditionalOnClass(name = "org.springframework.data.redis.cache.RedisCacheConfiguration")
	RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
		return builder -> {
			try {
				Field field = builder.getClass().getDeclaredField("defaultCacheConfiguration");
				field.setAccessible(true);
				RedisCacheConfiguration defaultCacheConfiguration = (RedisCacheConfiguration) field.get(builder);
				builder.cacheDefaults(defaultCacheConfiguration.serializeValuesWith(
						SerializationPair.fromSerializer(new CompactJdkSerializationRedisSerializer())));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		};
	}

}
