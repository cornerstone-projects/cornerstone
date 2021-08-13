package io.cornerstone.core.hibernate.criteria;

import static io.cornerstone.core.hibernate.criteria.PredicateBuilder.contains;
import static io.cornerstone.core.hibernate.criteria.PredicateBuilder.itemContains;
import static io.cornerstone.core.hibernate.criteria.PredicateBuilder.itemEndsWith;
import static io.cornerstone.core.hibernate.criteria.PredicateBuilder.itemStartsWith;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.cornerstone.test.DataJpaTestBase;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
public class PredicateBuilderTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Test
	public void testContains() {
		int size = 5;
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			Set<String> names = new HashSet<>();
			for (int j = 0; j < i + 1; j++)
				names.add("name" + j);
			entity.setNames(names);
			repository.save(entity);
		}
		for (int i = 0; i < size; i++) {
			String name = "name" + i;
			long expected = size - i;
			assertThat(repository.count((root, cq, cb) -> contains(root, cb, "names", name))).isEqualTo(expected);
		}
	}

	@Test
	public void testItemContains() {
		int size = 5;
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			Set<String> names = new HashSet<>();
			for (int j = 0; j < i + 1; j++)
				names.add("prefix" + "name" + j + "suffix");
			entity.setNames(names);
			repository.save(entity);
		}
		for (int i = 0; i < size; i++) {
			String name = "name" + i;
			long expected = size - i;
			assertThat(repository.count((root, cq, cb) -> itemContains(root, cb, "names", name))).isEqualTo(expected);
		}
	}

	@Test
	public void testItemStartsWith() {
		int size = 5;
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			Set<String> names = new HashSet<>();
			for (int j = 0; j < i + 1; j++)
				names.add("name" + j + "suffix");
			entity.setNames(names);
			repository.save(entity);
		}
		for (int i = 0; i < size; i++) {
			String name = "name" + i;
			long expected = size - i;
			assertThat(repository.count((root, cq, cb) -> itemStartsWith(root, cb, "names", name))).isEqualTo(expected);
		}
	}

	@Test
	public void testItemEndsWith() {
		int size = 5;
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			Set<String> names = new HashSet<>();
			for (int j = 0; j < i + 1; j++)
				names.add("prefix" + "name" + j);
			entity.setNames(names);
			repository.save(entity);
		}
		for (int i = 0; i < size; i++) {
			String name = "name" + i;
			long expected = size - i;
			assertThat(repository.count((root, cq, cb) -> itemEndsWith(root, cb, "names", name))).isEqualTo(expected);
		}
	}

}
