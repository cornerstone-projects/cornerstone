package io.cornerstone.core.redis.serializer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.data.redis.serializer.RedisSerializer;

public class JsonRedisSerializerTests extends RedisSerializerTestBase {

	@Override
	@Test
	@Disabled
	void testDateAndTime() {
		super.testDateAndTime();
	}

	@Override
	protected RedisSerializer<Object> getRedisSerializer() {
		return new JsonRedisSerializer<>();
	}

}
