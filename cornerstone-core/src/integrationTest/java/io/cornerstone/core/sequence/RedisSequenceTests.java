package io.cornerstone.core.sequence;

import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.core.sequence.CyclicSequence.CycleType;
import io.cornerstone.core.sequence.cyclic.RedisCyclicSequence;
import io.cornerstone.core.sequence.simple.RedisSimpleSequence;

@Import(RedisAutoConfiguration.class)
@ContextConfiguration(classes = RedisSequenceTests.Config.class)
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
