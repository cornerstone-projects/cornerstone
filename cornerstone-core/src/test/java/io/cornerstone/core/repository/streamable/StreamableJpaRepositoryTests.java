package io.cornerstone.core.repository.streamable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class StreamableJpaRepositoryTests extends DataJpaTestBase {

	@Autowired
	TestEntityRepository repository;

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void streamWithoutExistingTransaction() {
		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(this::doStream);
		this.repository.deleteAll();
	}

	@Test
	void streamInExistingTransaction() {
		doStream();
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void forEachWithoutExistingTransaction() {
		doForEach();
		this.repository.deleteAll();
	}

	@Test
	void forEachInExistingTransaction() {
		doForEach();
	}

	private void doStream() {
		int size = 5;
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			entity.setIndex(i);
			this.repository.save(entity);
		}
		try (Stream<TestEntity> stream = this.repository.stream(Sort.by("index"))) {
			List<Integer> indexes = stream.map(TestEntity::getIndex).collect(Collectors.toList());
			assertThat(indexes).containsExactly(0, 1, 2, 3, 4);
		}

		try (Stream<TestEntity> stream = this.repository.stream((root, cq, cb) -> cb.ge(root.get("index"), 1),
				Sort.by("index"))) {
			List<Integer> indexes = stream.map(TestEntity::getIndex).collect(Collectors.toList());
			assertThat(indexes).containsExactly(1, 2, 3, 4);
		}

		TestEntity example = new TestEntity();
		example.setIndex(1);
		try (Stream<TestEntity> stream = this.repository.stream(Example.of(example), Sort.unsorted())) {
			List<Integer> indexes = stream.map(TestEntity::getIndex).collect(Collectors.toList());
			assertThat(indexes).containsExactly(1);
		}
	}

	private void doForEach() {
		int size = 5;
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			entity.setIndex(i);
			this.repository.save(entity);
		}
		List<Integer> list = new ArrayList<>();
		this.repository.forEach(Sort.by("index"), e -> list.add(e.getIndex()));
		assertThat(list).containsExactly(0, 1, 2, 3, 4);
		list.clear();

		this.repository.forEach((root, cq, cb) -> cb.ge(root.get("index"), 1), Sort.by("index"),
				e -> list.add(e.getIndex()));
		assertThat(list).containsExactly(1, 2, 3, 4);
		list.clear();

		TestEntity example = new TestEntity();
		example.setIndex(1);
		this.repository.forEach(Example.of(example), Sort.unsorted(), e -> list.add(e.getIndex()));
		assertThat(list).containsExactly(1);
	}

	@Test
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	void forEachModifyWithoutExistingTransaction() {
		doForEachModify();
		assertThat(this.repository.findAll(Sort.by("index"))).extracting("index").containsExactly(0, 1, 2, 3, 4);
		// not modified
		this.repository.deleteAll();
	}

	@Test
	void forEachModifyInExistingTransaction() {
		doForEachModify();
		assertThat(this.repository.findAll(Sort.by("index"))).extracting("index").containsExactly(1, 2, 3, 4, 5);
		// modified
	}

	private void doForEachModify() {
		int size = 5;
		for (int i = 0; i < size; i++) {
			TestEntity entity = new TestEntity();
			entity.setIndex(i);
			this.repository.save(entity);
		}
		AtomicInteger count = new AtomicInteger();
		int batchSize = 2; // hibernate.jdbc.batch_size
		this.repository.forEach(Sort.by("index"), e -> {
			e.setIndex(e.getIndex() + 1);
			if ((count.incrementAndGet() % batchSize) == 0) {
				this.repository.flush();
			}
		});
	}

}
