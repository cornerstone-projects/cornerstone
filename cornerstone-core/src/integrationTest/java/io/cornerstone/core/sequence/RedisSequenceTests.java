package io.cornerstone.core.sequence;

import io.cornerstone.core.sequence.CyclicSequence.CycleType;
import io.cornerstone.core.sequence.cyclic.RedisCyclicSequence;
import io.cornerstone.core.sequence.simple.RedisSimpleSequence;
import io.cornerstone.test.containers.Redis;

import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = { RedisSequenceTests.Config.class, Redis.class })
class RedisSequenceTests extends SequenceTestBase {

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

	}

}
