package io.cornerstone.test.containers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.redis.testcontainers.RedisContainer;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.testcontainers.service.connection.ServiceConnectionAutoConfiguration;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@ImportTestcontainers(UseRedisContainer.Containers.class)
@ImportAutoConfiguration({ ServiceConnectionAutoConfiguration.class, DataRedisAutoConfiguration.class })
public @interface UseRedisContainer {

	class Containers {

		@Container
		@ServiceConnection
		static RedisContainer container = new RedisContainer("redis");

	}

}
