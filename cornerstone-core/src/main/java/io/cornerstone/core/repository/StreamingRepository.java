package io.cornerstone.core.repository;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

public interface StreamingRepository<T> {

	Stream<T> streamAllBy(Sort sort);

	@Transactional(readOnly = true)
	default void forEach(Consumer<T> consumer) {
		forEach(Sort.unsorted(), consumer);
	}

	@Transactional(readOnly = true)
	default void forEach(Sort sort, Consumer<T> consumer) {
		// avoid org.springframework.dao.InvalidDataAccessApiUsageException
		// see JpaQueryExecution.StreamExecution::doExecute
		try (Stream<T> all = streamAllBy(sort)) {
			all.forEach(consumer);
		}
	}
}
