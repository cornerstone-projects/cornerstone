package io.cornerstone.test.containers;

import org.testcontainers.containers.GenericContainer;
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

@ImportAutoConfiguration({ RedisAutoConfiguration.class, ServiceConnectionAutoConfiguration.class })
@ImportTestcontainers(Redis.class)
public class Redis {

	@Container
	@ServiceConnection
	static GenericContainer<?> container = new GenericContainer<>("redis").withExposedPorts(6379);

	// replace RedisTemplate<Object, Object> from RedisAutoConfiguration
	@Bean
	public RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, ?> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		return container;
	}

}
