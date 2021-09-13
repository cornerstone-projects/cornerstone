package io.cornerstone.core.sequence.simple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

public class RedisSimpleSequence extends AbstractSimpleSequence {

	public static final String KEY_SEQUENCE = "seq:";

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	private BoundValueOperations<String, String> boundValueOperations;

	@Override
	public void afterPropertiesSet() {
		Assert.hasText(getSequenceName(), "sequenceName shouldn't be blank");
		Assert.isTrue(getPaddingLength() > 0, "paddingLength should large than 0");
		this.boundValueOperations = this.stringRedisTemplate.boundValueOps(KEY_SEQUENCE + getSequenceName());
		this.boundValueOperations.setIfAbsent("0");
	}

	@Override
	public void restart() {
		this.boundValueOperations.set("0");
	}

	@Override
	public long nextLongValue() {
		Long value = this.boundValueOperations.increment(1);
		if (value == null)
			throw new RuntimeException("Unexpected null");
		return value;
	}

}
