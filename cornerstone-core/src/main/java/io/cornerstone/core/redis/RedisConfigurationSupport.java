package io.cornerstone.core.redis;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.data.redis.autoconfigure.ClientResourcesBuilderCustomizer;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisConnectionDetails;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientOptionsBuilderCustomizer;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.util.ClassUtils;

public class RedisConfigurationSupport {

	private final Object lettuceConnectionConfiguration;

	RedisConfigurationSupport(DataRedisProperties properties,
			ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
			ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
			ObjectProvider<RedisStaticMasterReplicaConfiguration> masterReplicaConfiguration,
			DataRedisConnectionDetails connectionDetails) {
		try {
			Class<?> clazz = DataRedisAutoConfiguration.class;
			Class<?> configurationClass = ClassUtils.forName(clazz.getPackageName() + ".LettuceConnectionConfiguration",
					clazz.getClassLoader());
			Constructor<?> ctor = configurationClass.getDeclaredConstructor(
					RedisConfigurationSupport.class.getDeclaredConstructors()[0].getParameterTypes());
			ctor.setAccessible(true);
			this.lettuceConnectionConfiguration = ctor.newInstance(properties, standaloneConfigurationProvider,
					sentinelConfigurationProvider, clusterConfigurationProvider, masterReplicaConfiguration,
					connectionDetails);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	protected DefaultClientResources lettuceClientResources(
			ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
		try {
			Method m = this.lettuceConnectionConfiguration.getClass()
				.getDeclaredMethod(getCurrentMethodName(), ObjectProvider.class);
			m.setAccessible(true);
			return (DefaultClientResources) m.invoke(this.lettuceConnectionConfiguration, customizers);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	protected LettuceConnectionFactory redisConnectionFactory(
			ObjectProvider<LettuceClientConfigurationBuilderCustomizer> clientConfigurationBuilderCustomizers,
			ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers,
			ClientResources clientResources) {
		try {
			Method m = this.lettuceConnectionConfiguration.getClass()
				.getDeclaredMethod(getCurrentMethodName(), ObjectProvider.class, ObjectProvider.class,
						ClientResources.class);
			m.setAccessible(true);
			return (LettuceConnectionFactory) m.invoke(this.lettuceConnectionConfiguration,
					clientConfigurationBuilderCustomizers, clientOptionsBuilderCustomizers, clientResources);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	protected RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

	protected StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return new StringRedisTemplate(redisConnectionFactory);
	}

	protected RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
			ObjectProvider<TaskExecutor> taskExecutor) {
		return createRedisMessageListenerContainer(redisConnectionFactory, taskExecutor);
	}

	static RedisMessageListenerContainer createRedisMessageListenerContainer(
			RedisConnectionFactory redisConnectionFactory, ObjectProvider<TaskExecutor> taskExecutor) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		taskExecutor.ifAvailable(container::setTaskExecutor);
		return container;
	}

	static DataRedisConnectionDetails createRedisConnectionDetails(DataRedisProperties properties,
			SslBundles sslBundles) {
		try {
			Constructor<?> ctor = ClassUtils
				.forName(DataRedisProperties.class.getPackageName() + ".PropertiesDataRedisConnectionDetails",
						DataRedisProperties.class.getClassLoader())
				.getDeclaredConstructor(DataRedisProperties.class, SslBundles.class);
			ctor.setAccessible(true);
			return (DataRedisConnectionDetails) ctor.newInstance(properties, sslBundles);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private static String getCurrentMethodName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}

}
