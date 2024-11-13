package io.cornerstone.core.redis;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.cornerstone.core.redis.DefaultRedisConfiguration.DefaultRedisProperties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@ConditionalOnProperty(prefix = DefaultRedisProperties.PREFIX, name = "enabled", havingValue = "true",
		matchIfMissing = true)
public @interface RedisEnabled {

}
