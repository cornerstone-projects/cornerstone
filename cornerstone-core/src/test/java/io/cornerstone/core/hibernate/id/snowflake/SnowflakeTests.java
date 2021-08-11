package io.cornerstone.core.hibernate.id.snowflake;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.cornerstone.core.hibernate.id.Snowflake;
import io.cornerstone.core.hibernate.id.SnowflakeProperties;
import io.cornerstone.test.DataJpaTestBase;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
public class SnowflakeTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Autowired
	SnowflakeProperties snowflakeProperties;

	@Test
	public void test() {
		Long id1 = repository.save(new TestEntity()).getId();
		Long id2 = repository.save(new TestEntity()).getId();
		assertThat(id1).isGreaterThan(100000000);
		assertThat(id2).isGreaterThan(id1);
		Snowflake sf = snowflakeProperties.build();
		assertThat(sf.parse(id1).getWorkerId()).isEqualTo(sf.parse(id2).getWorkerId());
		assertThat(sf.parse(id1).getTimestamp()).isLessThanOrEqualTo(sf.parse(id2).getTimestamp());
	}

}
