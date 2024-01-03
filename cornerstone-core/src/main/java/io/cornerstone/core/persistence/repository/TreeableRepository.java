package io.cornerstone.core.persistence.repository;

import java.util.List;

public interface TreeableRepository<T> {

	<S extends T> S save(S entity);

	<S extends T> S saveAndFlush(S entity);

	<S extends T> List<S> saveAll(Iterable<S> entities);

	<S extends T> List<S> saveAllAndFlush(Iterable<S> entities);

}
