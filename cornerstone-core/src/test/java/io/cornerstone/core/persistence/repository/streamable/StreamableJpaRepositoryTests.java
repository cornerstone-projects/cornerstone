package io.cornerstone.core.persistence.repository.streamable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.cornerstone.test.DataJpaTestBase;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static io.cornerstone.core.persistence.repository.streamable.TestEntity_.*;
import static org.assertj.core.api.Assertions.*;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StreamableJpaRepositoryTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Autowired
	EntityManager entityManager;

	@BeforeAll
	void prepare() {
		int size = 5;
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			entity.setIndex(i);
			this.repository.save(entity);
		}
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void streamWithoutExistingTransaction() {
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(this::doStream);
	}

	@Test
	void streamInExistingTransaction() {
		doStream();
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void forEachWithoutExistingTransaction() {
		doForEach();
	}

	@Test
	void forEachInExistingTransaction() {
		doForEach();
	}

	private void doStream() {
		try (Stream<TestEntity> stream = this.repository.stream(Sort.by(INDEX))) {
			List<Integer> indexes = stream.peek(this.entityManager::detach)
				.map(TestEntity::getIndex)
				.collect(Collectors.toList());
			assertThat(indexes).containsExactly(0, 1, 2, 3, 4);
		}

		try (Stream<TestEntity> stream = this.repository.stream((root, cq, cb) -> cb.ge(root.get(index), 1),
				Sort.by(INDEX))) {
			List<Integer> indexes = stream.peek(this.entityManager::detach)
				.map(TestEntity::getIndex)
				.collect(Collectors.toList());
			assertThat(indexes).containsExactly(1, 2, 3, 4);
		}

		TestEntity example = new TestEntity();
		example.setIndex(1);
		try (Stream<TestEntity> stream = this.repository.stream(Example.of(example), Sort.unsorted())) {
			List<Integer> indexes = stream.peek(this.entityManager::detach)
				.map(TestEntity::getIndex)
				.collect(Collectors.toList());
			assertThat(indexes).containsExactly(1);
		}
	}

	private void doForEach() {
		List<Integer> list = new ArrayList<>();
		this.repository.forEach(Sort.by(INDEX), e -> {
			this.entityManager.detach(e);
			list.add(e.getIndex());
		});
		assertThat(list).containsExactly(0, 1, 2, 3, 4);
		list.clear();

		this.repository.forEach((root, cq, cb) -> cb.ge(root.get(index), 1), Sort.by(INDEX), e -> {
			this.entityManager.detach(e);
			list.add(e.getIndex());
		});
		assertThat(list).containsExactly(1, 2, 3, 4);
		list.clear();

		TestEntity example = new TestEntity();
		example.setIndex(1);
		this.repository.forEach(Example.of(example), Sort.unsorted(), e -> {
			this.entityManager.detach(e);
			list.add(e.getIndex());
		});
		assertThat(list).containsExactly(1);
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void forEachModifyWithoutExistingTransaction() {
		doForEachModify();
		assertThat(this.repository.findAll(Sort.by(INDEX))).extracting(INDEX).containsExactly(0, 1, 2, 3, 4);
	}

	@Test
	void forEachModifyInExistingTransaction() {
		doForEachModify();
		assertThat(this.repository.findAll(Sort.by(INDEX))).extracting(INDEX).containsExactly(1, 2, 3, 4, 5);
	}

	private void doForEachModify() {
		AtomicInteger count = new AtomicInteger();
		int batchSize = 2; // hibernate.jdbc.batch_size
		this.repository.forEach(Sort.by(INDEX), e -> {
			e.setIndex(e.getIndex() + 1);
			if ((count.incrementAndGet() % batchSize) == 0) {
				this.repository.flush();
				this.entityManager.detach(e);
			}
		});
	}

}
