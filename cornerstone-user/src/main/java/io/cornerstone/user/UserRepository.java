package io.cornerstone.user;

import java.util.Optional;

import io.cornerstone.core.persistence.repository.StreamableJpaRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface UserRepository extends StreamableJpaRepository<User, Long> {

	String CACHE_NAME = "user";

	@Override
	@Transactional
	@Caching(evict = { @CacheEvict(cacheNames = CACHE_NAME, key = "#user.id"),
			@CacheEvict(cacheNames = CACHE_NAME, key = "#user.username") })
	<S extends User> S save(S user);

	@Override
	@Transactional
	@Caching(evict = { @CacheEvict(cacheNames = CACHE_NAME, key = "#user.id"),
			@CacheEvict(cacheNames = CACHE_NAME, key = "#user.username") })
	<S extends User> S saveAndFlush(S user);

	@Override
	@Transactional
	@Caching(evict = { @CacheEvict(cacheNames = CACHE_NAME, key = "#user.id"),
			@CacheEvict(cacheNames = CACHE_NAME, key = "#user.username") })
	void delete(User user);

	@Override
	@Cacheable(CACHE_NAME)
	Optional<User> findById(Long id);

	@Cacheable(CACHE_NAME)
	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);

}
