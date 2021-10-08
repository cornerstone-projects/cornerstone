package io.cornerstone.core.cache;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.lang.Nullable;

@Slf4j
public class DefaultCacheErrorHandler implements CacheErrorHandler {

	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		if (exception instanceof QueryTimeoutException) {
			log.error(exception.getMessage());
			return;
		}
		throw exception;
	}

	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, @Nullable Object value) {
		if (exception instanceof QueryTimeoutException) {
			log.error(exception.getMessage());
			return;
		}
		throw exception;
	}

	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		if (exception instanceof QueryTimeoutException) {
			log.error(exception.getMessage());
			return;
		}
		throw exception;
	}

	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		if (exception instanceof QueryTimeoutException) {
			log.error(exception.getMessage());
			return;
		}
		throw exception;
	}

}
