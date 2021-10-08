package io.cornerstone.test.containers;

import java.util.Optional;
import java.util.concurrent.Executor;

import org.testcontainers.containers.GenericContainer;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;

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
	public RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		template.setKeySerializer(RedisSerializer.string());
		template.setHashKeySerializer(RedisSerializer.string());
		return template;
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return new StringRedisTemplate(redisConnectionFactory);
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
			Optional<Executor> taskExecutor) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		taskExecutor.ifPresent(container::setTaskExecutor);
		return container;
	}

}
