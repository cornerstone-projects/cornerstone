package io.cornerstone.test.containers;

import io.cornerstone.core.redis.DefaultRedisConfiguration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration(proxyBeanMethods = false)
@Import(DefaultRedisConfiguration.class)
public class Redis extends AbstractContainer {

	@Override
	protected int getExposedPort() {
		return 6379;
	}

}
