package io.cornerstone.core.coordination;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.cornerstone.core.Application;
import io.cornerstone.core.coordination.impl.RedisLockService;
import io.cornerstone.core.coordination.impl.RedisMembership;

@Configuration(proxyBeanMethods = false)
@Profile("!test")
public class CoordinationConfiguration {

	@Bean
	LockService lockService(Application application, StringRedisTemplate stringRedisTemplate) {
		return new RedisLockService(application, stringRedisTemplate);
	}

	@Bean
	Membership membership(Application application, StringRedisTemplate stringRedisTemplate) {
		return new RedisMembership(application, stringRedisTemplate);
	}

}
