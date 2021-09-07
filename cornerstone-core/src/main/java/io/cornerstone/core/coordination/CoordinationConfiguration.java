package io.cornerstone.core.coordination;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.cornerstone.core.Application;
import io.cornerstone.core.coordination.impl.RedisLockService;
import io.cornerstone.core.coordination.impl.RedisMembership;
import io.cornerstone.core.coordination.impl.StandaloneLockService;
import io.cornerstone.core.coordination.impl.StandaloneMembership;
import io.cornerstone.core.redis.RedisEnabled;

@Configuration(proxyBeanMethods = false)
public class CoordinationConfiguration {

	@Bean
	@RedisEnabled
	LockService lockService(Application application, StringRedisTemplate stringRedisTemplate) {
		return new RedisLockService(application, stringRedisTemplate);
	}

	@Bean
	@RedisEnabled
	Membership membership(Application application, StringRedisTemplate stringRedisTemplate) {
		return new RedisMembership(application, stringRedisTemplate);
	}

	@Bean("lockService")
	@ConditionalOnMissingBean
	LockService standaloneLockService() {
		return new StandaloneLockService();
	}

	@Bean("membership")
	@ConditionalOnMissingBean
	Membership standaloneMembership(Application application) {
		return new StandaloneMembership(application);
	}
}
