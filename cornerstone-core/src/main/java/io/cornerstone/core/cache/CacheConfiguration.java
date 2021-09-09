package io.cornerstone.core.cache;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;

@Profile("!test")
@EnableCaching(order = Ordered.HIGHEST_PRECEDENCE + 3)
@Configuration(proxyBeanMethods = false)
public class CacheConfiguration extends CachingConfigurerSupport {

	@Override
	public CacheErrorHandler errorHandler() {
		return new DefaultCacheErrorHandler();
	}

}
