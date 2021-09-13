package io.cornerstone.core.hibernate.converter;

import static io.cornerstone.core.hibernate.converter.TestEnum.A;
import static io.cornerstone.core.hibernate.converter.TestEnum.B;
import static io.cornerstone.core.hibernate.converter.TestEnum.C;
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
class ConvertersTests extends DataJpaTestBase {

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
		entity.setEnumArray(new TestEnum[] { A, B, C });
		entity.setEnumList(Arrays.asList(entity.getEnumArray()));
		entity.setEnumSet(new LinkedHashSet<>(entity.getEnumList()));
		entity.setTestComponentList(Arrays.asList(new TestComponent("a", 1, new BigDecimal("10.1")),
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
