package io.cornerstone.core.redis;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.Executor;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.opentracing.contrib.redis.common.TracingConfiguration;
import io.opentracing.contrib.redis.spring.data2.connection.TracingRedisConnectionFactory;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import lombok.Getter;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
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

	@Getter
	private final RedisProperties properties;

	private final Object configuration;

	RedisConfigurationSupport(RedisProperties properties,
			ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
			ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
		this.properties = properties;
		try {
			Class<?> clazz = RedisAutoConfiguration.class;
			Class<?> configurationClass = ClassUtils.forName(clazz.getPackageName() + ".LettuceConnectionConfiguration",
					clazz.getClassLoader());
			Constructor<?> ctor = configurationClass.getDeclaredConstructor(RedisProperties.class, ObjectProvider.class,
					ObjectProvider.class, ObjectProvider.class);
			ctor.setAccessible(true);
			this.configuration = ctor.newInstance(properties, standaloneConfigurationProvider,
					sentinelConfigurationProvider, clusterConfigurationProvider);
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
			Optional<Executor> taskExecutor) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		taskExecutor.ifPresent(container::setTaskExecutor);
		return container;
	}

	protected RedisConnectionFactory wrap(RedisConnectionFactory redisConnectionFactory) {
		if (redisConnectionFactory instanceof TracingRedisConnectionFactory) {
			return redisConnectionFactory;
		}
		TracingConfiguration.Builder builder = new TracingConfiguration.Builder(GlobalTracer.get())
			.traceWithActiveSpanOnly(true)
			.extensionTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT);
		StringBuilder service = new StringBuilder("redis");
		RedisProperties properties = getProperties();
		if ((properties.getSentinel() != null) && (properties.getSentinel().getNodes() != null)) {
			properties.getSentinel().getNodes();
			builder.extensionTag("peer.address", String.join(",", properties.getSentinel().getNodes()));
			service.append("-sentinel");
		}
		else if ((properties.getCluster() != null) && (properties.getCluster().getNodes() != null)) {
			builder.extensionTag("peer.address", String.join(",", properties.getCluster().getNodes()));
			service.append("-cluster");
		}
		else {
			if (properties.isSsl()) {
				service.append("s");
			}
			service.append("://").append(properties.getHost());
			if (properties.getPort() != 6379) {
				service.append(":").append(properties.getPort());
			}
			if (properties.getDatabase() > 0) {
				service.append("/").append(properties.getDatabase());
			}
		}
		builder.extensionTag(Tags.PEER_SERVICE.getKey(), service.toString());
		return new TracingRedisConnectionFactory(redisConnectionFactory, builder.build());
	}

}
