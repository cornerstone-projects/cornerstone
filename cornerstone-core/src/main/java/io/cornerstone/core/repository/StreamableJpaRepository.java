package io.cornerstone.core.repository;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface StreamableJpaRepository<T, ID> extends JpaRepository<T, ID> {

	default Stream<T> stream(Sort sort) {
		return stream((Specification<T>) null, sort);
	}

	Stream<T> stream(@Nullable Specification<T> spec, Sort sort);

	<S extends T> Stream<S> stream(Example<S> spec, Sort sort);

	@Transactional(readOnly = true)
	default void forEach(Consumer<T> consumer) {
		forEach(Sort.unsorted(), consumer);
	}

	@Transactional(readOnly = true)
	default void forEach(Sort sort, Consumer<T> consumer) {
		forEach((Specification<T>) null, sort, consumer);
	}

	@Transactional(readOnly = true)
	default void forEach(@Nullable Specification<T> spec, Sort sort, Consumer<T> consumer) {
		// avoid org.springframework.dao.InvalidDataAccessApiUsageException
		// see JpaQueryExecution.StreamExecution::doExecute
		try (Stream<T> all = stream(spec, sort)) {
			all.forEach(consumer);
		}
	}

	@Transactional(readOnly = true)
	default <S extends T> void forEach(Example<S> example, Sort sort, Consumer<S> consumer) {
		// avoid org.springframework.dao.InvalidDataAccessApiUsageException
		// see JpaQueryExecution.StreamExecution::doExecute
		try (Stream<S> all = stream(example, sort)) {
			all.forEach(consumer);
		}
	}
}
