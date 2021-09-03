package io.cornerstone.test.containers;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
public class Redis extends AbstractContainer {

	@Override
	protected int getExposedPort() {
		return 6379;
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
