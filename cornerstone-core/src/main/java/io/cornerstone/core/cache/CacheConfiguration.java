package io.cornerstone.core.cache;

import io.cornerstone.core.redis.serializer.CompactJdkSerializationRedisSerializer;
import io.cornerstone.core.util.ReflectionUtils;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

@Profile("!test")
@EnableCaching(order = Ordered.HIGHEST_PRECEDENCE + 3)
@Configuration(proxyBeanMethods = false)
public class CacheConfiguration implements CachingConfigurer {

	@Override
	public CacheErrorHandler errorHandler() {
		return new LoggingCacheErrorHandler(LogFactory.getLog(LoggingCacheErrorHandler.class), true);
	}

	@Bean
	@ConditionalOnClass(name = "org.springframework.data.redis.cache.RedisCacheConfiguration")
	RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
		return builder -> {
			RedisCacheConfiguration oldDefaultCacheConfiguration = ReflectionUtils.getFieldValue(builder,
					"defaultCacheConfiguration");
			RedisCacheConfiguration newDefaultCacheConfiguration = oldDefaultCacheConfiguration
				.serializeValuesWith(SerializationPair.fromSerializer(new CompactJdkSerializationRedisSerializer()));
			builder.cacheDefaults(newDefaultCacheConfiguration);
		};
	}

}
