package io.cornerstone.core.hibernate.type;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.cornerstone.test.DataJpaTestBase;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
public class JsonTypeTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Test
	public void test() {
		TestEntity entity = new TestEntity();
		entity.setTestComponentList(Arrays.asList(new TestComponent("a", 1, new BigDecimal("10.1")),
				new TestComponent("b", 2, new BigDecimal("10.2")), new TestComponent("c", 3, new BigDecimal("10.3"))));
		repository.save(entity);
		flushAndClear();

		Long id = entity.getId();
		assertThat(id).isNotNull();
		TestEntity savedEntity = repository.findById(id).orElseThrow(IllegalStateException::new);
		assertThat(savedEntity).isNotSameAs(entity);
		assertThat(savedEntity).isEqualTo(entity);
	}

}
