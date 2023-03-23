package io.cornerstone.core.redis;

import java.util.Optional;
import java.util.concurrent.Executor;

import io.cornerstone.core.redis.DefaultRedisConfiguration.DefaultRedisProperties;
import io.cornerstone.core.redis.GlobalRedisConfiguration.GlobalRedisProperties;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GlobalRedisProperties.class)
@ConditionalOnProperty(prefix = GlobalRedisProperties.PREFIX, name = "enabled", havingValue = "true")
public class GlobalRedisConfiguration extends RedisConfigurationSupport {

	GlobalRedisConfiguration(GlobalRedisProperties properties,
			ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
			ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
		super(properties, standaloneConfigurationProvider, sentinelConfigurationProvider, clusterConfigurationProvider);
	}

	@Bean(destroyMethod = "shutdown")
	public DefaultClientResources globalLettuceClientResources(
			ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
		return super.lettuceClientResources(customizers);
	}

	@Bean
	public LettuceConnectionFactory globalRedisConnectionFactory(
			ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
			@Qualifier("globalLettuceClientResources") ClientResources lettuceClientResources) {
		return super.redisConnectionFactory(builderCustomizers, lettuceClientResources);
	}

	@Bean
	public RedisTemplate<String, ?> globalRedisTemplate(
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

	@ConfigurationProperties(prefix = GlobalRedisProperties.PREFIX)
	public static class GlobalRedisProperties extends RedisProperties {

		public static final String PREFIX = "global.data.redis";

		@Autowired
		public GlobalRedisProperties(DefaultRedisProperties defaultRedisProperties) {
			BeanUtils.copyProperties(defaultRedisProperties, this);
		}

	}

}
