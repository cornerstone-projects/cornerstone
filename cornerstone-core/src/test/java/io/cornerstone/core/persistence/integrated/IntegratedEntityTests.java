package io.cornerstone.core.persistence.integrated;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static io.cornerstone.core.persistence.integrated.TestEntity.TestEnum.*;
import static org.assertj.core.api.Assertions.assertThat;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class IntegratedEntityTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Test
	void test() {
		TestEntity entity = new TestEntity();
		entity.setStringArray(new String[] { "a", "b", "c" });
		entity.setStringList(List.of(entity.getStringArray()));
		entity.setStringSet(new LinkedHashSet<>(entity.getStringList()));
		entity.setStringMap(Collections.singletonMap("key", "value"));
		entity.setIntegerArray(new Integer[] { 1, 2, 3 });
		entity.setIntegerList(List.of(entity.getIntegerArray()));
		entity.setIntegerSet(new LinkedHashSet<>(entity.getIntegerList()));
		entity.setLongArray(new Long[] { 1L, 2L, 3L });
		entity.setLongList(List.of(entity.getLongArray()));
		entity.setLongSet(new LinkedHashSet<>(entity.getLongList()));
		entity.setEnumArray(new TestEntity.TestEnum[] { A, B, C });
		entity.setEnumList(List.of(entity.getEnumArray()));
		entity.setEnumSet(new LinkedHashSet<>(entity.getEnumList()));
		entity.setTestComponentList(List.of(new TestEntity.TestComponent("a", 1, new BigDecimal("10.1")),
				new TestEntity.TestComponent("b", 2, new BigDecimal("10.2")),
				new TestEntity.TestComponent("c", 3, new BigDecimal("10.3"))));
		entity.setAnotherComponentList(List.of(new TestEntity.AnotherComponent("a", 1, new BigDecimal("10.1")),
				new TestEntity.AnotherComponent("b", 2, new BigDecimal("10.2")),
				new TestEntity.AnotherComponent("c", 3, new BigDecimal("10.3"))));
		this.repository.save(entity);
		flushAndClear();

		Long id = entity.getId();
		assertThat(id).isGreaterThan(100000000L);
		TestEntity savedEntity = this.repository.findById(id).orElseThrow(IllegalStateException::new);
		assertThat(savedEntity).isNotSameAs(entity);
		assertThat(savedEntity).isEqualTo(entity);

	}

}
