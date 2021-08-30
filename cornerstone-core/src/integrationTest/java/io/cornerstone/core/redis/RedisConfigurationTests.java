package io.cornerstone.core.redis;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.cornerstone.test.SpringApplicationTestBase;

@TestPropertySource(properties = { "spring.redis.enabled=true", "spring.redis.database=1", "global.redis.enabled=true",
		"global.redis.database=2" })
@Testcontainers
public class RedisConfigurationTests extends SpringApplicationTestBase {

	@Container
	static GenericContainer<?> container = new GenericContainer<>("redis").withExposedPorts(6379);

	@DynamicPropertySource
	static void registerDynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host", container::getHost);
		registry.add("spring.redis.port", container::getFirstMappedPort);
		registry.add("global.redis.host", container::getHost);
		registry.add("global.redis.port", container::getFirstMappedPort);
	}

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private StringRedisTemplate globalStringRedisTemplate;

	@Test
	void test() {
		assertThat(stringRedisTemplate).isNotSameAs(globalStringRedisTemplate);
		String key = "test";
		ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
		ValueOperations<String, String> globalOps = globalStringRedisTemplate.opsForValue();
		ops.set(key, "stringRedisTemplate");
		globalOps.set(key, "globalStringRedisTemplate");
		assertThat(ops.get(key)).isNotEqualTo(globalOps.get(key));
	}

}
