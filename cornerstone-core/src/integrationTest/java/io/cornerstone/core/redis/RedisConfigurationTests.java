package io.cornerstone.core.redis;

import javax.annotation.Resource;

import io.cornerstone.core.redis.DefaultRedisConfiguration.DefaultRedisProperties;
import io.cornerstone.core.redis.GlobalRedisConfiguration.GlobalRedisProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = { DefaultRedisConfiguration.class, GlobalRedisConfiguration.class })
@TestPropertySource(
		properties = { "spring.redis.enabled=true", "spring.redis.database=1", "spring.redis.client-name=default",
				"global.redis.enabled=true", "global.redis.database=2", "global.redis.client-name=global" })
@Testcontainers
@ExtendWith(SpringExtension.class)
class RedisConfigurationTests {

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
		assertThat(this.globalRedisProperties.getHost()).isEqualTo(this.defaultRedisProperties.getHost());
		assertThat(this.globalRedisProperties.getPort()).isEqualTo(this.defaultRedisProperties.getPort());
		assertThat(this.globalRedisProperties.getDatabase()).isNotEqualTo(this.defaultRedisProperties.getDatabase());
		assertThat(this.globalRedisProperties.getClientName())
			.isNotEqualTo(this.defaultRedisProperties.getClientName());
	}

	@Test
	void testRedisTemplate() {
		assertThat(this.redisTemplate).isNotSameAs(this.globalRedisTemplate);
		String key = "test";
		ValueOperations<String, Object> ops = this.redisTemplate.opsForValue();
		ValueOperations<String, Object> globalOps = this.globalRedisTemplate.opsForValue();
		ops.set(key, "redisTemplate");
		globalOps.set(key, "globalRedisTemplate");
		assertThat(ops.get(key)).isNotEqualTo(globalOps.get(key));
	}

	@Test
	void testStringRedisTemplate() {
		assertThat(this.stringRedisTemplate).isNotSameAs(this.globalStringRedisTemplate);
		String key = "test";
		ValueOperations<String, String> ops = this.stringRedisTemplate.opsForValue();
		ValueOperations<String, String> globalOps = this.globalStringRedisTemplate.opsForValue();
		ops.set(key, "stringRedisTemplate");
		globalOps.set(key, "globalStringRedisTemplate");
		assertThat(ops.get(key)).isNotEqualTo(globalOps.get(key));
	}

}
