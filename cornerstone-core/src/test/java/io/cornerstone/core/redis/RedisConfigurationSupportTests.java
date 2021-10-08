package io.cornerstone.core.redis;

import io.lettuce.core.resource.DefaultClientResources;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
class RedisConfigurationSupportTests {

	@Test
	void test() {
		RedisConfigurationSupport rcs = new RedisConfigurationSupport(new RedisProperties(), mock(ObjectProvider.class),
				mock(ObjectProvider.class));
		DefaultClientResources clientResources = rcs.lettuceClientResources();
		assertThat(clientResources).isNotNull();
		assertThat(rcs.redisConnectionFactory(mock(ObjectProvider.class), clientResources)).isNotNull();
	}

}