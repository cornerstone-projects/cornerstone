package io.cornerstone.core.redis.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;

public class CompactJdkSerializationRedisSerializerTests extends RedisSerializerTestBase {

	@Override
	protected RedisSerializer<Object> getRedisSerializer() {
		return new CompactJdkSerializationRedisSerializer();
	}

}
