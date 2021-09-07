package io.cornerstone.core.redis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import io.cornerstone.core.redis.DefaultRedisConfiguration.DefaultRedisProperties;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ConditionalOnProperty(prefix = DefaultRedisProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public @interface RedisEnabled {

}
