package io.cornerstone.core.redis;

import java.util.Optional;
import java.util.concurrent.Executor;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import io.cornerstone.core.redis.DefaultRedisConfiguration.DefaultRedisProperties;
import io.cornerstone.core.redis.GlobalRedisConfiguration.GlobalRedisProperties;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = GlobalRedisProperties.PREFIX, name = "enabled", havingValue = "true")
public class GlobalRedisConfiguration extends RedisConfigurationSupport {

	GlobalRedisConfiguration(GlobalRedisProperties properties,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
			ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
		super(properties, sentinelConfigurationProvider, clusterConfigurationProvider);
	}

	@Bean(destroyMethod = "shutdown")
	public DefaultClientResources globalLettuceClientResources() {
		return super.lettuceClientResources();
	}

	@Bean
	public LettuceConnectionFactory rawGlobalRedisConnectionFactory(
			ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
			@Qualifier("globalLettuceClientResources") ClientResources lettuceClientResources) {
		return super.redisConnectionFactory(builderCustomizers, lettuceClientResources);
	}

	@Bean
	public RedisConnectionFactory globalRedisConnectionFactory(
			@Qualifier("rawGlobalRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		return wrap(redisConnectionFactory);
	}

	@Bean
	public RedisTemplate<Object, Object> globalRedisTemplate(
			@Qualifier("globalRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		return super.redisTemplate(redisConnectionFactory);
	}

	@Bean
	public StringRedisTemplate globalStringRedisTemplate(
			@Qualifier("globalRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		return super.stringRedisTemplate(redisConnectionFactory);
	}

	@Bean
	public RedisMessageListenerContainer globalRedisMessageListenerContainer(
			@Qualifier("globalRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory,
			Optional<Executor> taskExecutor) {
		return super.redisMessageListenerContainer(redisConnectionFactory, taskExecutor);
	}

	@Component
	@ConfigurationProperties(prefix = GlobalRedisProperties.PREFIX)
	public static class GlobalRedisProperties extends RedisProperties {

		public static final String PREFIX = "global.redis";

		public GlobalRedisProperties(DefaultRedisProperties defaultRedisProperties) {
			BeanUtils.copyProperties(defaultRedisProperties, this);
		}

	}

}
