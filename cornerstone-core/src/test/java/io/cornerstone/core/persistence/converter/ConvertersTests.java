package io.cornerstone.core.persistence.converter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import io.cornerstone.test.DataJpaTestBase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.hibernate.type.BasicType;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static io.cornerstone.core.persistence.converter.TestEnum.A;
import static io.cornerstone.core.persistence.converter.TestEnum.B;
import static io.cornerstone.core.persistence.converter.TestEnum.C;
import static org.assertj.core.api.Assertions.assertThat;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class ConvertersTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Autowired
	EntityManager entityManager;

	@Test
	void testMetamodel() {
		EntityType<TestEntity> entityType = this.entityManager.getMetamodel().entity(TestEntity.class);
		List.of("stringArray", "stringList", "stringSet", "stringMap", "integerArray", "integerList", "integerSet",
				"longArray", "longList", "longSet", "enumArray", "enumList", "enumSet", "testComponentList")
			.forEach(name -> assertThat(entityType.getAttribute(name)).isInstanceOfSatisfying(SingularAttribute.class,
					sa -> assertThat(sa.getType()).isInstanceOfSatisfying(BasicType.class,
							bt -> assertThat(bt.getJdbcJavaType().getJavaType()).isEqualTo(String.class))));
	}

	@Test
	void testSaveAndGet() {
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
		entity.setEnumArray(new TestEnum[] { A, B, C });
		entity.setEnumList(List.of(entity.getEnumArray()));
		entity.setEnumSet(new LinkedHashSet<>(entity.getEnumList()));
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
