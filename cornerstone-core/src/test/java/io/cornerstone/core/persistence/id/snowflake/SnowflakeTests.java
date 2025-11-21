package io.cornerstone.core.persistence.id.snowflake;

import io.cornerstone.core.persistence.id.Snowflake;
import io.cornerstone.core.persistence.id.SnowflakeProperties;
import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class SnowflakeTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Autowired
	SnowflakeProperties snowflakeProperties;

	@Test
	void test() {
		Long id1 = this.repository.save(new TestEntity()).getId();
		Long id2 = this.repository.save(new TestEntity()).getId();
		assertThat(id1).isNotNull();
		assertThat(id2).isNotNull();
		assertThat(id1).isGreaterThan(100000000);
		assertThat(id2).isGreaterThan(id1);
		Snowflake sf = this.snowflakeProperties.build();
		assertThat(sf.parse(id1).workerId()).isEqualTo(sf.parse(id2).workerId());
		assertThat(sf.parse(id1).timestamp()).isLessThanOrEqualTo(sf.parse(id2).timestamp());
	}

	@Test
	void testAssignedIdentifier() {
		TestEntity entity = new TestEntity();
		Long id = this.snowflakeProperties.build().nextId();
		entity.setId(id);
		Long savedId = this.repository.save(entity).getId();
		assertThat(savedId).isEqualTo(id);
	}

}
