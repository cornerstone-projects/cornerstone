package io.cornerstone.test.containers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.redis.testcontainers.RedisContainer;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.testcontainers.service.connection.ServiceConnectionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@ImportTestcontainers(UseRedisContainer.Containers.class)
@ImportAutoConfiguration({ ServiceConnectionAutoConfiguration.class, UseRedisContainer.Config.class,
		RedisAutoConfiguration.class })
public @interface UseRedisContainer {

	class Containers {

		@Container
		@ServiceConnection
		static RedisContainer container = new RedisContainer("redis");

	}

	class Config {

		@Bean
		RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
			RedisTemplate<String, ?> template = new RedisTemplate<>();
			template.setConnectionFactory(redisConnectionFactory);
			return template;
		}

		@Bean
		RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
			RedisMessageListenerContainer container = new RedisMessageListenerContainer();
			container.setConnectionFactory(redisConnectionFactory);
			return container;
		}

	}

}
