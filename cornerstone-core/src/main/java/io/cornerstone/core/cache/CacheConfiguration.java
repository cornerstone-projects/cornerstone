package io.cornerstone.core.cache;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@EnableCaching
@Configuration(proxyBeanMethods = false)
public class CacheConfiguration extends CachingConfigurerSupport {

	@Override
	public CacheErrorHandler errorHandler() {
		return new DefaultCacheErrorHandler();
	}

}
