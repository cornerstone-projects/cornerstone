package io.cornerstone.core.redis;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.cornerstone.core.redis.DefaultRedisConfiguration.DefaultRedisProperties;
import io.cornerstone.core.redis.GlobalRedisConfiguration.GlobalRedisProperties;

@ContextConfiguration(classes = { DefaultRedisConfiguration.class, GlobalRedisConfiguration.class })
@TestPropertySource(properties = { "spring.redis.enabled=true", "spring.redis.database=1",
		"spring.redis.client-name=default", "global.redis.enabled=true", "global.redis.database=2",
		"global.redis.client-name=global" })
@Testcontainers
@ExtendWith(SpringExtension.class)
public class RedisConfigurationTests {

	@Container
	static GenericContainer<?> container = new GenericContainer<>("redis").withExposedPorts(6379);

	@DynamicPropertySource
	static void registerDynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host", container::getHost);
		registry.add("spring.redis.port", container::getFirstMappedPort);
	}

	@Autowired
	private DefaultRedisProperties defaultRedisProperties;

	@Autowired
	private GlobalRedisProperties globalRedisProperties;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Resource
	private RedisTemplate<String, Object> globalRedisTemplate;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private StringRedisTemplate globalStringRedisTemplate;

	@Test
	void testRedisProperties() {
		assertThat(globalRedisProperties.getHost()).isEqualTo(defaultRedisProperties.getHost());
		assertThat(globalRedisProperties.getPort()).isEqualTo(defaultRedisProperties.getPort());
		assertThat(globalRedisProperties.getDatabase()).isNotEqualTo(defaultRedisProperties.getDatabase());
		assertThat(globalRedisProperties.getClientName()).isNotEqualTo(defaultRedisProperties.getClientName());
	}

	@Test
	void testRedisTemplate() {
		assertThat(redisTemplate).isNotSameAs(globalRedisTemplate);
		String key = "test";
		ValueOperations<String, Object> ops = redisTemplate.opsForValue();
		ValueOperations<String, Object> globalOps = globalRedisTemplate.opsForValue();
		ops.set(key, "redisTemplate");
		globalOps.set(key, "globalRedisTemplate");
		assertThat(ops.get(key)).isNotEqualTo(globalOps.get(key));
	}

	@Test
	void testStringRedisTemplate() {
		assertThat(stringRedisTemplate).isNotSameAs(globalStringRedisTemplate);
		String key = "test";
		ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
		ValueOperations<String, String> globalOps = globalStringRedisTemplate.opsForValue();
		ops.set(key, "stringRedisTemplate");
		globalOps.set(key, "globalStringRedisTemplate");
		assertThat(ops.get(key)).isNotEqualTo(globalOps.get(key));
	}

}
