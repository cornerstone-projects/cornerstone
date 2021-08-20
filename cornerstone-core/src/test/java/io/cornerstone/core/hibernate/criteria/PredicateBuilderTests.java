package io.cornerstone.core.hibernate.criteria;

import static io.cornerstone.core.hibernate.criteria.PredicateBuilder.contains;
import static io.cornerstone.core.hibernate.criteria.PredicateBuilder.isConstantFalse;
import static io.cornerstone.core.hibernate.criteria.PredicateBuilder.isConstantTrue;
import static io.cornerstone.core.hibernate.criteria.PredicateBuilder.itemContains;
import static io.cornerstone.core.hibernate.criteria.PredicateBuilder.itemEndsWith;
import static io.cornerstone.core.hibernate.criteria.PredicateBuilder.itemStartsWith;
import static io.cornerstone.core.hibernate.criteria.PredicateBuilder.regexpLike;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.cornerstone.test.DataJpaTestBase;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class PredicateBuilderTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Test
	void testIsConstant() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
		Root<TestEntity> root = cq.from(TestEntity.class);
		assertThat(isConstantTrue(cb.isTrue(cb.literal(true)))).isTrue();
		assertThat(isConstantTrue(cb.isFalse(cb.literal(false)))).isTrue();
		assertThat(isConstantTrue(cb.isNotNull(cb.literal("")))).isTrue();
		assertThat(isConstantTrue(cb.isTrue(root.get("enabled")))).isFalse();
		assertThat(isConstantTrue(cb.isFalse(root.get("enabled")))).isFalse();
		assertThat(isConstantTrue(cb.isNotNull(root.get("name")))).isFalse();

		assertThat(isConstantFalse(cb.isTrue(cb.literal(false)))).isTrue();
		assertThat(isConstantFalse(cb.isFalse(cb.literal(true)))).isTrue();
		assertThat(isConstantFalse(cb.isNull(cb.literal("")))).isTrue();
		assertThat(isConstantFalse(cb.isTrue(root.get("enabled")))).isFalse();
		assertThat(isConstantFalse(cb.isFalse(root.get("enabled")))).isFalse();
		assertThat(isConstantFalse(cb.isNotNull(root.get("name")))).isFalse();
	}

	@Test
	void testExample() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<TestEntity> cq = cb.createQuery(TestEntity.class);
		Root<TestEntity> root = cq.from(TestEntity.class);
		ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("name", match -> match.contains());
		Predicate predicate = cb.isNotNull(root.get("name"));
		TestEntity example = new TestEntity();
		assertThat(PredicateBuilder.andExample(root, cb, predicate, Example.of(example, matcher))).isSameAs(predicate);
		assertThat(isConstantTrue(PredicateBuilder.orExample(root, cb, predicate, Example.of(example, matcher))))
				.isTrue();
		example.setEnabled(true);
		Predicate predicate2 = PredicateBuilder.andExample(root, cb, predicate, Example.of(example, matcher));
		assertThat(predicate2).isNotSameAs(predicate);
		assertThat(isConstantTrue(predicate2)).isFalse();
		assertThat(isConstantFalse(predicate2)).isFalse();
		predicate2 = PredicateBuilder.orExample(root, cb, predicate, Example.of(example, matcher));
		assertThat(predicate2).isNotSameAs(predicate);
		assertThat(isConstantTrue(predicate2)).isFalse();
		assertThat(isConstantFalse(predicate2)).isFalse();
	}

	@Test
	void testContains() {
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
	void testItemContains() {
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
	void testItemStartsWith() {
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
	void testItemEndsWith() {
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

	@Test
	void testRegexpLike() {
		int size = 5;
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			List<String> list = new ArrayList<>();
			for (int j = 0; j < i + 1; j++)
				list.add(String.valueOf(j));
			entity.setName("name" + String.join("", list));
			repository.save(entity);
		}
		for (int i = 0; i < size; i++) {
			List<String> list = new ArrayList<>();
			for (int j = 0; j < i + 1; j++)
				list.add(String.valueOf(j));
			String name = "name" + String.join("", list) + ".*";
			long expected = size - i;
			assertThat(repository.count((root, cq, cb) -> regexpLike(root, cb, "name", name))).isEqualTo(expected);
		}
	}

}
