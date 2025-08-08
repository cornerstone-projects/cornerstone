package io.cornerstone.core.redis;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientOptionsBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(RedisProperties.class)
@ConditionalOnProperty(prefix = GlobalRedisConfiguration.PREFIX, name = "enabled", havingValue = "true")
public class GlobalRedisConfiguration extends RedisConfigurationSupport {

	public static final String PREFIX = "global.data.redis";

	@Bean(defaultCandidate = false)
	public static RedisConnectionDetails globalRedisConnectionDetails(
			@Qualifier("globalRedisProperties") RedisProperties redisProperties,
			ObjectProvider<SslBundles> sslBundles) {
		return createRedisConnectionDetails(redisProperties, sslBundles.getIfAvailable());
	}

	@ConfigurationProperties(PREFIX)
	@Bean(defaultCandidate = false)
	public static RedisProperties globalRedisProperties(RedisProperties redisProperties) {
		RedisProperties globalRedisProperties = new RedisProperties();
		// inherit from "spring.data.redis" prefix
		BeanUtils.copyProperties(redisProperties, globalRedisProperties);
		return globalRedisProperties;
	}

	GlobalRedisConfiguration(@Qualifier("globalRedisProperties") RedisProperties redisProperties,
			ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
			ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
			@Qualifier("globalRedisConnectionDetails") RedisConnectionDetails redisConnectionDetails) {
		super(redisProperties, standaloneConfigurationProvider, sentinelConfigurationProvider,
				clusterConfigurationProvider, redisConnectionDetails);
	}

	@Bean(defaultCandidate = false, destroyMethod = "shutdown")
	public DefaultClientResources globalLettuceClientResources(
			ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
		return super.lettuceClientResources(customizers);
	}

	@Bean(defaultCandidate = false)
	public LettuceConnectionFactory globalRedisConnectionFactory(
			ObjectProvider<LettuceClientConfigurationBuilderCustomizer> clientConfigurationBuilderCustomizers,
			ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers,
			@Qualifier("globalLettuceClientResources") ClientResources lettuceClientResources) {
		return super.redisConnectionFactory(clientConfigurationBuilderCustomizers, clientOptionsBuilderCustomizers,
				lettuceClientResources);
	}

	@Bean(defaultCandidate = false)
	public RedisTemplate<Object, Object> globalRedisTemplate(
			@Qualifier("globalRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		return super.redisTemplate(redisConnectionFactory);
	}

	@Bean(defaultCandidate = false)
	public StringRedisTemplate globalStringRedisTemplate(
			@Qualifier("globalRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		return super.stringRedisTemplate(redisConnectionFactory);
	}

	@Bean(defaultCandidate = false)
	public RedisMessageListenerContainer globalRedisMessageListenerContainer(
			@Qualifier("globalRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory,
			@Qualifier("applicationTaskExecutor") ObjectProvider<TaskExecutor> taskExecutor) {
		return super.redisMessageListenerContainer(redisConnectionFactory, taskExecutor);
	}

}
