package io.cornerstone.core.redis;

import io.lettuce.core.resource.DefaultClientResources;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
class RedisConfigurationSupportTests {

	@Test
	void test() {
		RedisProperties properties = new RedisProperties();
		RedisConnectionDetails connectionDetails = RedisConfigurationSupport.redisConnectionDetails(properties);
		RedisConfigurationSupport rcs = new RedisConfigurationSupport(properties, mock(ObjectProvider.class),
				mock(ObjectProvider.class), mock(ObjectProvider.class), connectionDetails, mock(ObjectProvider.class));
		DefaultClientResources clientResources = rcs.lettuceClientResources(mock(ObjectProvider.class));
		assertThat(clientResources).isNotNull();
		assertThat(rcs.redisConnectionFactory(mock(ObjectProvider.class), clientResources)).isNotNull();
	}

}
