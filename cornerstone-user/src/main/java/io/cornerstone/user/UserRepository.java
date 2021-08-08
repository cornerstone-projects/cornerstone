package io.cornerstone.user;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

	String CACHE_NAME = "user";

	@Override
	@Caching(evict = { @CacheEvict(cacheNames = CACHE_NAME, key = "#user.id"),
			@CacheEvict(cacheNames = CACHE_NAME, key = "#user.username") })
	<S extends User> S save(S user);

	@Override
	@Caching(evict = { @CacheEvict(cacheNames = CACHE_NAME, key = "#user.id"),
			@CacheEvict(cacheNames = CACHE_NAME, key = "#user.username") })
	<S extends User> S saveAndFlush(S user);

	@Override
	@Caching(evict = { @CacheEvict(cacheNames = CACHE_NAME, key = "#user.id"),
			@CacheEvict(cacheNames = CACHE_NAME, key = "#user.username") })
	void delete(User user);

	@Override
	@Cacheable(CACHE_NAME)
	User getById(Long id);

	@Override
	@Cacheable(CACHE_NAME)
	Optional<User> findById(Long id);

	@Cacheable(CACHE_NAME)
	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);

	Stream<User> findBy(Sort sort);

	@Transactional(readOnly = true)
	default void iterate(Sort sort, Consumer<User> consumer) {
		// avoid org.springframework.dao.InvalidDataAccessApiUsageException
		// see JpaQueryExecution.StreamExecution::doExecute
		try (Stream<User> all = findBy(sort)) {
			all.forEach(consumer);
		}
	}

}
