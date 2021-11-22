package io.cornerstone.core.redis;

import java.util.Optional;
import java.util.concurrent.Executor;

import io.cornerstone.core.redis.DefaultRedisConfiguration.DefaultRedisProperties;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DefaultRedisProperties.class)
@RedisEnabled
public class DefaultRedisConfiguration extends RedisConfigurationSupport {

	DefaultRedisConfiguration(DefaultRedisProperties properties,
			ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
			ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
		super(properties, standaloneConfigurationProvider, sentinelConfigurationProvider, clusterConfigurationProvider);
	}

	@Primary
	@Bean(destroyMethod = "shutdown")
	@Override
	public DefaultClientResources lettuceClientResources(ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
		return super.lettuceClientResources(customizers);
	}

	@Bean
	public LettuceConnectionFactory rawRedisConnectionFactory(
			ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
			ClientResources lettuceClientResources) {
		return super.redisConnectionFactory(builderCustomizers, lettuceClientResources);
	}

	@Bean(name = "redisConnectionFactory")
	@Primary
	public RedisConnectionFactory wrappedRedisConnectionFactory(
			@Qualifier("rawRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		return wrap(redisConnectionFactory);
	}

	@Bean
	@Primary
	@Override
	public RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return super.redisTemplate(redisConnectionFactory);
	}

	@Bean
	@Override
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return super.stringRedisTemplate(redisConnectionFactory);
	}

	@Bean
	@Primary
	@Override
	public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
			Optional<Executor> taskExecutor) {
		return super.redisMessageListenerContainer(redisConnectionFactory, taskExecutor);
	}

	@Primary
	@ConfigurationProperties(prefix = DefaultRedisProperties.PREFIX)
	public static class DefaultRedisProperties extends RedisProperties {

		public static final String PREFIX = "spring.redis";

	}

}
