package io.cornerstone.core.redis;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.ClassUtils;

public class RedisConfigurationSupport {

	private final Object configuration;

	RedisConfigurationSupport(RedisProperties properties,
			ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
			ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
			RedisConnectionDetails connectionDetails, ObjectProvider<SslBundles> sslBundles) {
		try {
			Class<?> clazz = RedisAutoConfiguration.class;
			Class<?> configurationClass = ClassUtils.forName(clazz.getPackageName() + ".LettuceConnectionConfiguration",
					clazz.getClassLoader());
			Constructor<?> ctor = configurationClass.getDeclaredConstructor(
					RedisConfigurationSupport.class.getDeclaredConstructors()[0].getParameterTypes());
			ctor.setAccessible(true);
			this.configuration = ctor.newInstance(properties, standaloneConfigurationProvider,
					sentinelConfigurationProvider, clusterConfigurationProvider, connectionDetails, sslBundles);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	protected DefaultClientResources lettuceClientResources(
			ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
		try {
			Method m = this.configuration.getClass().getDeclaredMethod("lettuceClientResources", ObjectProvider.class);
			m.setAccessible(true);
			return (DefaultClientResources) m.invoke(this.configuration, customizers);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	protected LettuceConnectionFactory redisConnectionFactory(
			ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers,
			ClientResources clientResources) {
		try {
			Method m = this.configuration.getClass()
				.getDeclaredMethod("redisConnectionFactory", ObjectProvider.class, ClientResources.class);
			m.setAccessible(true);
			return (LettuceConnectionFactory) m.invoke(this.configuration, builderCustomizers, clientResources);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	protected RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		template.setKeySerializer(RedisSerializer.string());
		template.setHashKeySerializer(RedisSerializer.string());
		return template;
	}

	protected StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return new StringRedisTemplate(redisConnectionFactory);
	}

	protected RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
			ObjectProvider<Executor> taskExecutor) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		taskExecutor.ifAvailable(container::setTaskExecutor);
		return container;
	}

	protected static RedisConnectionDetails redisConnectionDetails(RedisProperties properties) {
		try {
			Constructor<?> ctor = ClassUtils
				.forName(RedisProperties.class.getPackageName() + ".PropertiesRedisConnectionDetails",
						RedisProperties.class.getClassLoader())
				.getDeclaredConstructor(RedisProperties.class);
			ctor.setAccessible(true);
			return (RedisConnectionDetails) ctor.newInstance(properties);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

}
