package io.cornerstone.core.hibernate.id.sequence;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.cornerstone.core.sequence.CyclicSequence.CycleType;
import io.cornerstone.core.hibernate.id.SnowflakeProperties;
import io.cornerstone.core.sequence.Sequence;
import io.cornerstone.core.sequence.cyclic.DatabaseCyclicSequenceDelegate;
import io.cornerstone.test.DataJpaTestBase;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
@Import(SequenceTests.Config.class)
public class SequenceTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Autowired
	SnowflakeProperties snowflakeProperties;

	@Test
	public void test() {
		Long id1 = repository.save(new TestEntity()).getId();
		Long id2 = repository.save(new TestEntity()).getId();
		assertThat(id2).isEqualTo(id1 + 1);

	}

	static class Config {

		@Bean
		public Sequence testSequence(DataSource dataSource) {
			DatabaseCyclicSequenceDelegate cs = new DatabaseCyclicSequenceDelegate(dataSource);
			cs.setCycleType(CycleType.MINUTE);
			return cs;
		}

	}
}
