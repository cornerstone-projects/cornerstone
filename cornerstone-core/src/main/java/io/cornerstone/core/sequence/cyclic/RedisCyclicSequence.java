package io.cornerstone.core.sequence.cyclic;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.Assert;

import io.cornerstone.core.util.MaxAttemptsExceededException;

public class RedisCyclicSequence extends AbstractCyclicSequence {

	public static final String KEY_SEQUENCE = "seq:";

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	private BoundValueOperations<String, String> boundValueOperations;

	private RedisScript<Boolean> compareAndSetScript = new DefaultRedisScript<>(
			"if redis.call('get',KEYS[1]) == ARGV[1] then redis.call('set',KEYS[1],ARGV[2]) return true else return false end",
			Boolean.class);

	@Override
	public void afterPropertiesSet() {
		Assert.hasText(getSequenceName(), "sequenceName shouldn't be blank");
		Assert.isTrue(getPaddingLength() > 0, "paddingLength should large than 0");
		int maxlength = String.valueOf(Long.MAX_VALUE).length() - getCycleType().getPattern().length();
		Assert.isTrue(getPaddingLength() <= maxlength, "paddingLength should not large than " + maxlength);
		boundValueOperations = stringRedisTemplate.boundValueOps(KEY_SEQUENCE + getSequenceName());
		Long time = stringRedisTemplate.execute((RedisConnection connection) -> connection.time());
		if (time == null)
			throw new RuntimeException("Unexpected null");
		boundValueOperations.setIfAbsent(getStringValue(new Date(time), getPaddingLength(), 0));
	}

	@Override
	public String nextStringValue() {
		int maxAttempts = 3;
		int remainingAttempts = maxAttempts;
		do {
			@SuppressWarnings("unchecked")
			byte[] key = ((RedisSerializer<String>) stringRedisTemplate.getKeySerializer())
					.serialize(boundValueOperations.getKey());
			List<Object> results = stringRedisTemplate.executePipelined((RedisConnection connection) -> {
				connection.incr(key);
				connection.time();
				return null;
			});
			long value = (Long) results.get(0);
			Date now = new Date((Long) results.get(1));
			final String stringValue = String.valueOf(value);
			if (stringValue.length() == getPaddingLength() + getCycleType().getPattern().length()) {
				Calendar cal = Calendar.getInstance();
				CycleType cycleType = getCycleType();
				if (cycleType.ordinal() <= CycleType.MINUTE.ordinal())
					cal.set(Calendar.MINUTE, Integer.parseInt(stringValue.substring(10, 12)));
				if (cycleType.ordinal() <= CycleType.HOUR.ordinal())
					cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(stringValue.substring(8, 10)));
				if (cycleType.ordinal() <= CycleType.DAY.ordinal())
					cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(stringValue.substring(6, 8)));
				if (cycleType.ordinal() <= CycleType.MONTH.ordinal())
					cal.set(Calendar.MONTH, Integer.parseInt(stringValue.substring(4, 6)) - 1);
				if (cycleType.ordinal() <= CycleType.YEAR.ordinal())
					cal.set(Calendar.YEAR, Integer.parseInt(stringValue.substring(0, 4)));
				Date d = cal.getTime();
				if (getCycleType().isSameCycle(d, now))
					return stringValue;
				else if (d.after(now)) {
					// treat it as overflow not clock jumps backward
					long next = value
							- Long.valueOf(CycleType.DAY.format(now)) * ((long) Math.pow(10, getPaddingLength()));
					return cycleType.format(now) + next;
				}
			}
			String restart = getStringValue(now, getPaddingLength(), 1);
			Boolean success = stringRedisTemplate.execute(compareAndSetScript,
					Collections.singletonList(boundValueOperations.getKey()), stringValue, restart);
			if (success == null)
				throw new RuntimeException("Unexpected null");
			if (success)
				return restart;
			try {
				Thread.sleep((1 + maxAttempts - remainingAttempts) * 50);
			} catch (InterruptedException e) {
				logger.warn(e.getMessage(), e);
			}
		} while (--remainingAttempts > 0);
		throw new MaxAttemptsExceededException(maxAttempts);
	}

}