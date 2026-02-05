package io.cornerstone.core.redis;

import io.lettuce.core.resource.DefaultClientResources;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.data.redis.autoconfigure.DataRedisConnectionDetails;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
class RedisConfigurationSupportTests {

	@Test
	void test() {
		DataRedisProperties properties = new DataRedisProperties();
		DataRedisConnectionDetails connectionDetails = RedisConfigurationSupport
			.createRedisConnectionDetails(properties, null);
		RedisConfigurationSupport rcs = new RedisConfigurationSupport(properties, mock(ObjectProvider.class),
				mock(ObjectProvider.class), mock(ObjectProvider.class), mock(ObjectProvider.class), connectionDetails);
		DefaultClientResources clientResources = rcs.lettuceClientResources(mock(ObjectProvider.class));
		RedisConnectionFactory connectionFactory = rcs.redisConnectionFactory(mock(ObjectProvider.class),
				mock(ObjectProvider.class), clientResources);
		rcs.redisTemplate(connectionFactory);
		rcs.stringRedisTemplate(connectionFactory);
	}

}
