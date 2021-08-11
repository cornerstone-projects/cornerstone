package io.cornerstone.core.sequence;

import org.junit.jupiter.api.Disabled;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import io.cornerstone.core.sequence.CyclicSequence.CycleType;
import io.cornerstone.core.sequence.cyclic.RedisCyclicSequence;
import io.cornerstone.core.sequence.simple.RedisSimpleSequence;

@Disabled
@Import({ RedisAutoConfiguration.class, RedisSequenceTests.Config.class })
public class RedisSequenceTests extends SequenceTestBase {

	static class Config {

		@Bean
		public Sequence sample1Sequence() {
			return new RedisSimpleSequence();
		}

		@Bean
		public Sequence sample2Sequence() {
			RedisCyclicSequence cs = new RedisCyclicSequence();
			cs.setCycleType(CycleType.MINUTE);
			cs.setPaddingLength(7);
			return cs;
		}
	}

}
