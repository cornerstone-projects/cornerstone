package io.cornerstone.core.persistence.type;

import java.math.BigDecimal;
import java.util.List;

import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class JsonTypeTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Test
	void test() {
		TestEntity entity = new TestEntity();
		entity.setTestComponentList(List.of(new TestComponent("a", 1, new BigDecimal("10.1")),
				new TestComponent("b", 2, new BigDecimal("10.2")), new TestComponent("c", 3, new BigDecimal("10.3"))));
		this.repository.save(entity);
		flushAndClear();

		Long id = entity.getId();
		assertThat(id).isNotNull();
		TestEntity savedEntity = this.repository.findById(id).orElseThrow(IllegalStateException::new);
		assertThat(savedEntity).isNotSameAs(entity);
		assertThat(savedEntity).isEqualTo(entity);
	}

}
