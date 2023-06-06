package io.cornerstone.core.repository.scrollable;

import java.util.Collections;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.cornerstone.test.DataJpaTestBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.support.WindowIterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class WindowIteratorIntegrationTests extends DataJpaTestBase {

	private static final int pageSize = 10;

	private static final String[][] sortKeys = new String[][] { null, { "id" }, { "seqNo" }, { "seqNo", "id" } };

	private static final Integer[] totals = new Integer[] { 0, 5, 10, 15, 20, 25 };

	@Autowired
	TestEntityRepository repository;

	void prepare(int total) {
		for (int i = 0; i < total; i++) {
			TestEntity entity = new TestEntity();
			entity.setSeqNo(i);
			this.repository.save(entity);
		}
	}

	@ParameterizedTest
	@MethodSource("cartesian")
	void iterate(String[] keys, Sort.Direction sortDirection, ScrollPosition.Direction scrollDirection, int total) {

		prepare(total);

		Sort sort;
		if (keys != null) {
			sort = Sort.by(sortDirection, keys);
		}
		else {
			sort = Sort.unsorted();
			// implicit "id:ASC" will be used
			assumeTrue(sortDirection == Sort.Direction.ASC);
		}

		KeysetScrollPosition position = ScrollPosition.of(Map.of(), scrollDirection);
		if (scrollDirection == ScrollPosition.Direction.BACKWARD
				&& position.getDirection() == ScrollPosition.Direction.FORWARD) {
			// remove this workaround if
			// https://github.com/spring-projects/spring-data-commons/pull/2841 merged
			position = position.backward();
		}

		WindowIterator<TestEntity> iterator = WindowIterator
			.of(p -> this.repository.findBy((root, query, cb) -> null, q -> q.limit(pageSize).sortBy(sort).scroll(p)))
			.startingAt(position);

		assertIterator(total, iterator,
				sortDirection == Sort.Direction.ASC && scrollDirection == ScrollPosition.Direction.FORWARD
						|| sortDirection == Sort.Direction.DESC
								&& scrollDirection == ScrollPosition.Direction.BACKWARD);
	}

	private void assertIterator(int total, WindowIterator<TestEntity> iterator, boolean asc) {
		Stream<Integer> stream = IntStream.range(0, total).boxed();
		if (!asc) {
			stream = stream.sorted(Collections.reverseOrder());
		}
		assertThat(iterator).toIterable().map(TestEntity::getSeqNo).containsExactly(stream.toArray(Integer[]::new));
	}

	private static Stream<Arguments> cartesian() {
		return Stream.of(sortKeys)
			.flatMap(keys -> Stream.of(Sort.Direction.class.getEnumConstants())
				.flatMap(sortDirection -> Stream.of(ScrollPosition.Direction.class.getEnumConstants())
					.flatMap(scrollDirection -> Stream.of(totals)
						.map(total -> Arguments.of(keys, sortDirection, scrollDirection, total)))));
	}

}
