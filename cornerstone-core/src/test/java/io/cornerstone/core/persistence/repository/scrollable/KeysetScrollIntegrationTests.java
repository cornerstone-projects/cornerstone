package io.cornerstone.core.persistence.repository.scrollable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@EnableJpaRepositories(basePackageClasses = TestEntityRepository.class)
@EntityScan(basePackageClasses = TestEntity.class)
class KeysetScrollIntegrationTests extends DataJpaTestBase {

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
	void scroll(String[] keys, Sort.Direction sortDirection, ScrollPosition.Direction scrollDirection, int total) {

		prepare(total);

		List<List<TestEntity>> contents = new ArrayList<>();

		Sort sort;
		if (keys != null) {
			sort = Sort.by(sortDirection, keys);
		}
		else {
			sort = Sort.unsorted();
			// implicit "id:ASC" will be used
			assumeTrue(sortDirection == Sort.Direction.ASC);
		}

		KeysetScrollPosition position = ScrollPosition.of(Collections.emptyMap(), scrollDirection);
		while (true) {
			ScrollPosition positionToUse = position;
			Window<TestEntity> window = this.repository.findBy((root, query, cb) -> null,
					q -> q.limit(pageSize).sortBy(sort).scroll(positionToUse));
			if (!window.isEmpty()) {
				contents.add(window.getContent());
			}
			if (!window.hasNext()) {
				break;
			}
			int indexForNext = position.scrollsForward() ? window.size() - 1 : 0;
			position = (KeysetScrollPosition) window.positionAt(indexForNext);
		}

		if (total == 0) {
			assertThat(contents).hasSize(0);
			return;
		}

		boolean divisible = total % pageSize == 0;

		assertThat(contents).hasSize(divisible ? total / pageSize : total / pageSize + 1);
		for (int i = 0; i < contents.size() - 1; i++) {
			assertThat(contents.get(i)).hasSize(pageSize);
		}
		if (divisible) {
			assertThat(contents.getLast()).hasSize(pageSize);
		}
		else {
			assertThat(contents.getLast()).hasSize(total % pageSize);
		}

		List<TestEntity> first = contents.getFirst();
		List<TestEntity> last = contents.getLast();

		if (sortDirection == Sort.Direction.ASC) {
			if (scrollDirection == ScrollPosition.Direction.FORWARD) {
				assertThat(first.getFirst().getSeqNo()).isEqualTo(0);
				assertThat(last.getLast().getSeqNo()).isEqualTo(total - 1);
			}
			else {
				assertThat(first.getLast().getSeqNo()).isEqualTo(total - 1);
				assertThat(last.getFirst().getSeqNo()).isEqualTo(0);
			}
		}
		else {
			if (scrollDirection == ScrollPosition.Direction.FORWARD) {
				assertThat(first.getFirst().getSeqNo()).isEqualTo(total - 1);
				assertThat(last.getLast().getSeqNo()).isEqualTo(0);
			}
			else {
				assertThat(first.getLast().getSeqNo()).isEqualTo(0);
				assertThat(last.getFirst().getSeqNo()).isEqualTo(total - 1);
			}
		}
	}

	private static Stream<Arguments> cartesian() {
		return Stream.of(sortKeys)
			.flatMap(keys -> Stream.of(Sort.Direction.class.getEnumConstants())
				.flatMap(sortDirection -> Stream.of(ScrollPosition.Direction.class.getEnumConstants())
					.flatMap(scrollDirection -> Stream.of(totals)
						.map(total -> Arguments.of(keys, sortDirection, scrollDirection, total)))));
	}

}
