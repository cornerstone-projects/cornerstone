package io.cornerstone.test.containers;

import io.cornerstone.core.redis.DefaultRedisConfiguration;
import io.cornerstone.core.redis.DefaultRedisConfiguration.DefaultRedisProperties;
import org.testcontainers.containers.GenericContainer;

import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
@Import(DefaultRedisConfiguration.class)
public class Redis extends AbstractContainer {

	@Override
	protected int getExposedPort() {
		return 6379;
	}

	@Primary
	@Bean
	public DefaultRedisProperties defaultRedisProperties(GenericContainer<?> redisContainer) {
		DefaultRedisProperties properties = new DefaultRedisProperties();
		PropertyMapper map = PropertyMapper.get();
		map.from(redisContainer::getHost).to(properties::setHost);
		map.from(redisContainer::getFirstMappedPort).to(properties::setPort);
		return properties;
	}

}
