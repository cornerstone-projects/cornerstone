package io.cornerstone.core.repository;

import java.util.stream.Stream;

import javax.persistence.EntityManager;

import lombok.Setter;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.core.support.SurroundingTransactionDetectorMethodInterceptor;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

@Transactional(readOnly = true)
public class SimpleStreamableJpaRepository<T, ID> extends SimpleJpaRepository<T, ID>
		implements StreamableJpaRepository<T, ID> {

	// see JpaQueryExecution.StreamExecution
	private static final String NO_SURROUNDING_TRANSACTION = "You're trying to execute a streaming query method without a surrounding transaction that keeps the connection open so that the Stream can actually be consumed. Make sure the code consuming the stream uses @Transactional or any other way of declaring a (read-only) transaction.";

	@Setter
	private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;

	SimpleStreamableJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
		super(entityInformation, entityManager);
	}

	@Override
	@Transactional(readOnly = true, propagation = SUPPORTS)
	public Stream<T> stream(@Nullable Specification<T> spec, Sort sort) {
		if (!SurroundingTransactionDetectorMethodInterceptor.INSTANCE.isSurroundingTransactionActive()) {
			throw new InvalidDataAccessApiUsageException(NO_SURROUNDING_TRANSACTION);
		}
		return getQuery(spec, sort).getResultStream();
	}

	@Override
	@Transactional(readOnly = true, propagation = SUPPORTS)
	public <S extends T> Stream<S> stream(@Nullable Example<S> example, Sort sort) {
		if (!SurroundingTransactionDetectorMethodInterceptor.INSTANCE.isSurroundingTransactionActive()) {
			throw new InvalidDataAccessApiUsageException(NO_SURROUNDING_TRANSACTION);
		}
		return getQuery(
				(root, cq, cb) -> QueryByExamplePredicateBuilder.getPredicate(root, cb, example, this.escapeCharacter),
				example.getProbeType(), sort).getResultStream();
	}

}
