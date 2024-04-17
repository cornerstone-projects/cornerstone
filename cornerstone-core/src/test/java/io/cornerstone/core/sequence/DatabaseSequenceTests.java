package io.cornerstone.core.sequence;

import javax.sql.DataSource;

import io.cornerstone.core.sequence.CyclicSequence.CycleType;
import io.cornerstone.core.sequence.cyclic.DatabaseCyclicSequenceDelegate;
import io.cornerstone.core.sequence.simple.DatabaseSimpleSequenceDelegate;
import lombok.Getter;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = DatabaseSequenceTests.Config.class)
@ImportAutoConfiguration(DataSourceAutoConfiguration.class)
class DatabaseSequenceTests extends SequenceTestBase {

	@Getter
	private final int threads = 5;

	@Getter
	private final int loop = 1000;

	static class Config {

		@Bean
		Sequence sample1Sequence(DataSource dataSource) {
			return new DatabaseSimpleSequenceDelegate(dataSource);
		}

		@Bean
		Sequence sample2Sequence(DataSource dataSource) {
			DatabaseCyclicSequenceDelegate cs = new DatabaseCyclicSequenceDelegate(dataSource);
			cs.setCycleType(CycleType.MINUTE);
			return cs;
		}

	}

}
