package io.cornerstone.core.sequence;

import java.time.LocalDateTime;
import java.time.Period;

import io.cornerstone.core.sequence.CyclicSequence.CycleType;
import io.cornerstone.core.sequence.cyclic.RedisCyclicSequence;
import io.cornerstone.core.sequence.simple.RedisSimpleSequence;
import io.cornerstone.test.containers.UseRedisContainer;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@UseRedisContainer
@ContextConfiguration
class RedisSequenceTests extends SequenceTestBase {

	@Autowired
	private RedisCyclicSequence sample3Sequence;

	@Autowired
	private BoundValueOperations<String, String> sample3SequenceOperations;

	@Test
	void testCrossCycle() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime yesterday = now.minus(Period.ofDays(1));
		String currentCycle = this.sample3Sequence.getCycleType().format(now);
		String lastCycle = this.sample3Sequence.getCycleType().format(yesterday);
		this.sample3SequenceOperations.set(lastCycle + "9998");
		assertThat(this.sample3Sequence.nextStringValue()).isEqualTo(currentCycle + "0001");

		this.sample3SequenceOperations.set(lastCycle + "9999");
		assertThat(this.sample3Sequence.nextStringValue()).isEqualTo(currentCycle + "0001");
	}

	@Test
	void testOverflowToNextCycle() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime tomorrow = now.plus(Period.ofDays(1));
		String currentCycle = this.sample3Sequence.getCycleType().format(now);
		String nextCycle = this.sample3Sequence.getCycleType().format(tomorrow);
		this.sample3SequenceOperations.set(currentCycle + "9999");
		assertThat(this.sample3Sequence.nextStringValue()).isEqualTo(currentCycle + "10000");
		assertThat(this.sample3SequenceOperations.get()).isEqualTo(nextCycle + "0000");
		assertThat(this.sample3Sequence.nextStringValue()).isEqualTo(currentCycle + "10001");
		assertThat(this.sample3SequenceOperations.get()).isEqualTo(nextCycle + "0001");
	}

	@Test
	void testOverflowToInvalidDate() {
		// 20230229 is invalid date
		this.sample3SequenceOperations.set("202302290001");
		this.sample3Sequence.nextStringValue();
	}

	@Configuration
	static class Config {

		@Bean
		Sequence sample1Sequence() {
			return new RedisSimpleSequence();
		}

		@Bean
		Sequence sample2Sequence() {
			RedisCyclicSequence cs = new RedisCyclicSequence();
			cs.setCycleType(CycleType.MINUTE);
			cs.setPaddingLength(7);
			return cs;
		}

		@Bean
		RedisCyclicSequence sample3Sequence() {
			RedisCyclicSequence cs = new RedisCyclicSequence();
			cs.setCycleType(CycleType.DAY);
			cs.setPaddingLength(4);
			return cs;
		}

		@Bean
		BoundValueOperations<String, String> sample3SequenceOperations(StringRedisTemplate redisTemplate,
				RedisCyclicSequence sample3Sequence) {
			return redisTemplate.boundValueOps(RedisCyclicSequence.KEY_SEQUENCE + sample3Sequence.getSequenceName());
		}

	}

}
