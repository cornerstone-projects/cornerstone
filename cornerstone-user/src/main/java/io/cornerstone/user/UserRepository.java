package io.cornerstone.user;

import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.cornerstone.core.repository.StreamingRepository;

public interface UserRepository
		extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>, StreamingRepository<User> {

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

}
