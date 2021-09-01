package io.cornerstone.test.containers;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;

public class Redis {

	private static final String DOCKER_IMAGE = "redis:latest";

	@Bean
	public GenericContainer<?> redisContainer() {
		@SuppressWarnings("resource")
		GenericContainer<?> container = new GenericContainer<>(DOCKER_IMAGE).withExposedPorts(6379);
		container.start();
		return container;
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory(GenericContainer<?> redisContainer) {
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(
				redisContainer.getContainerIpAddress(), redisContainer.getFirstMappedPort());
		return new LettuceConnectionFactory(configuration);
	}

	@Bean
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return new StringRedisTemplate(redisConnectionFactory);
	}

}
