package io.cornerstone.core.cache;

import com.fasterxml.jackson.core.JsonParseException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.lang.Nullable;

@Slf4j
public class DefaultCacheErrorHandler implements CacheErrorHandler {

	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		if (exception instanceof SerializationFailedException
				|| exception.getCause() instanceof SerializationFailedException) {
			return;
		}
		if (exception.getCause() instanceof JsonParseException) {
			return;
		}
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
