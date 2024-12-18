package io.cornerstone.core.coordination;

import io.cornerstone.core.coordination.impl.RedisLockService;
import io.cornerstone.core.coordination.impl.RedisMembership;
import io.cornerstone.core.coordination.impl.StandaloneLockService;
import io.cornerstone.core.coordination.impl.StandaloneMembership;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
public class CoordinationConfiguration {

	@Bean
	@ConditionalOnBean(StringRedisTemplate.class)
	LockService lockService(StringRedisTemplate stringRedisTemplate) {
		return new RedisLockService(stringRedisTemplate);
	}

	@Bean
	@ConditionalOnBean(StringRedisTemplate.class)
	Membership membership(StringRedisTemplate stringRedisTemplate) {
		return new RedisMembership(stringRedisTemplate);
	}

	@Bean("lockService")
	@ConditionalOnMissingBean
	LockService standaloneLockService() {
		return new StandaloneLockService();
	}

	@Bean("membership")
	@ConditionalOnMissingBean
	Membership standaloneMembership() {
		return new StandaloneMembership();
	}

}
