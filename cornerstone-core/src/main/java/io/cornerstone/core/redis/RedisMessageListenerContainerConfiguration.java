package io.cornerstone.core.redis;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisMessageListenerContainerConfiguration {

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
			ObjectProvider<Executor> taskExecutor) {
		return RedisConfigurationSupport.createRedisMessageListenerContainer(redisConnectionFactory, taskExecutor);
	}

}
