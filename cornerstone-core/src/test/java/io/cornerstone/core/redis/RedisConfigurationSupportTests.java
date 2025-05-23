package io.cornerstone.core.redis;

import io.lettuce.core.resource.DefaultClientResources;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
class RedisConfigurationSupportTests {

	@Test
	void test() {
		RedisProperties properties = new RedisProperties();
		RedisConnectionDetails connectionDetails = RedisConfigurationSupport.createRedisConnectionDetails(properties,
				null);
		RedisConfigurationSupport rcs = new RedisConfigurationSupport(properties, mock(ObjectProvider.class),
				mock(ObjectProvider.class), mock(ObjectProvider.class), connectionDetails, mock(ObjectProvider.class));
		DefaultClientResources clientResources = rcs.lettuceClientResources(mock(ObjectProvider.class));
		RedisConnectionFactory connectionFactory = rcs.redisConnectionFactory(mock(ObjectProvider.class),
				mock(ObjectProvider.class), clientResources);
		rcs.redisTemplate(connectionFactory);
		rcs.stringRedisTemplate(connectionFactory);
	}

}
