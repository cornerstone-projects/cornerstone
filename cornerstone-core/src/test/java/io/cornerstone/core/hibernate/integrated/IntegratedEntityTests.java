package io.cornerstone.core.hibernate.integrated;

import static io.cornerstone.core.hibernate.integrated.TestEntity.TestEnum.A;
import static io.cornerstone.core.hibernate.integrated.TestEntity.TestEnum.B;
import static io.cornerstone.core.hibernate.integrated.TestEntity.TestEnum.C;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.cornerstone.test.DataJpaTestBase;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class IntegratedEntityTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Test
	void test() {
		TestEntity entity = new TestEntity();
		entity.setStringArray(new String[] { "a", "b", "c" });
		entity.setStringList(Arrays.asList(entity.getStringArray()));
		entity.setStringSet(new LinkedHashSet<>(entity.getStringList()));
		entity.setStringMap(Collections.singletonMap("key", "value"));
		entity.setIntegerArray(new Integer[] { 1, 2, 3 });
		entity.setIntegerList(Arrays.asList(entity.getIntegerArray()));
		entity.setIntegerSet(new LinkedHashSet<>(entity.getIntegerList()));
		entity.setLongArray(new Long[] { 1L, 2L, 3L });
		entity.setLongList(Arrays.asList(entity.getLongArray()));
		entity.setLongSet(new LinkedHashSet<>(entity.getLongList()));
		entity.setEnumArray(new TestEntity.TestEnum[] { A, B, C });
		entity.setEnumList(Arrays.asList(entity.getEnumArray()));
		entity.setEnumSet(new LinkedHashSet<>(entity.getEnumList()));
		entity.setTestComponentList(Arrays.asList(new TestEntity.TestComponent("a", 1, new BigDecimal("10.1")),
				new TestEntity.TestComponent("b", 2, new BigDecimal("10.2")),
				new TestEntity.TestComponent("c", 3, new BigDecimal("10.3"))));
		entity.setAnotherComponentList(Arrays.asList(new TestEntity.AnotherComponent("a", 1, new BigDecimal("10.1")),
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
