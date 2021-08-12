package io.cornerstone.core.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.cornerstone.test.DataJpaTestBase;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class, repositoryBaseClass = SimpleStreamableJpaRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
public class StreamableJpaRepositoryTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void streamWithoutExistingTransaction() {
		assertThatThrownBy(this::doStream).isInstanceOf(InvalidDataAccessApiUsageException.class);
		repository.deleteAll();
	}

	@Test
	public void streamInExistingTransaction() {
		doStream();
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void forEachWithoutExistingTransaction() {
		doForEach();
		repository.deleteAll();
	}

	@Test
	public void forEachInExistingTransaction() {
		doForEach();
	}

	private void doStream() {
		int size = 5;
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			entity.setIndex(i);
			repository.save(entity);
		}
		try (Stream<TestEntity> stream = repository.stream(Sort.by("index"))) {
			List<Integer> indexes = stream.map(TestEntity::getIndex).collect(Collectors.toList());
			assertThat(indexes).containsExactly(0, 1, 2, 3, 4);
		}

		try (Stream<TestEntity> stream = repository.stream((root, cq, cb) -> cb.ge(root.get("index"), 1),
				Sort.by("index"))) {
			List<Integer> indexes = stream.map(TestEntity::getIndex).collect(Collectors.toList());
			assertThat(indexes).containsExactly(1, 2, 3, 4);
		}

		TestEntity example = new TestEntity();
		example.setIndex(1);
		try (Stream<TestEntity> stream = repository.stream(Example.of(example), Sort.unsorted())) {
			List<Integer> indexes = stream.map(TestEntity::getIndex).collect(Collectors.toList());
			assertThat(indexes).containsExactly(1);
		}
	}

	private void doForEach() {
		int size = 5;
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			entity.setIndex(i);
			repository.save(entity);
		}
		List<Integer> list = new ArrayList<>();
		repository.forEach(Sort.by("index"), e -> list.add(e.getIndex()));
		assertThat(list).containsExactly(0, 1, 2, 3, 4);
		list.clear();

		repository.forEach((root, cq, cb) -> cb.ge(root.get("index"), 1), Sort.by("index"),
				e -> list.add(e.getIndex()));
		assertThat(list).containsExactly(1, 2, 3, 4);
		list.clear();

		TestEntity example = new TestEntity();
		example.setIndex(1);
		repository.forEach(Example.of(example), Sort.unsorted(), e -> list.add(e.getIndex()));
		assertThat(list).containsExactly(1);
	}

}
