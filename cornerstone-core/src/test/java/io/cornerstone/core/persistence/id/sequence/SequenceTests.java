package io.cornerstone.core.persistence.id.sequence;

import javax.sql.DataSource;

import io.cornerstone.core.persistence.id.SnowflakeProperties;
import io.cornerstone.core.sequence.CyclicSequence.CycleType;
import io.cornerstone.core.sequence.Sequence;
import io.cornerstone.core.sequence.cyclic.DatabaseCyclicSequenceDelegate;
import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
@ContextConfiguration
class SequenceTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Autowired
	SnowflakeProperties snowflakeProperties;

	@Test
	void test() {
		Long id1 = this.repository.save(new TestEntity()).getId();
		Long id2 = this.repository.save(new TestEntity()).getId();
		assertThat(id2).isNotNull();
		assertThat(id2).isEqualTo(id1 + 1);

	}

	@Configuration
	static class Config {

		@Bean
		Sequence testSequence(DataSource dataSource) {
			DatabaseCyclicSequenceDelegate cs = new DatabaseCyclicSequenceDelegate(dataSource);
			cs.setCycleType(CycleType.MINUTE);
			return cs;
		}

	}

}
