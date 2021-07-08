package com.example.demo.core.coordination;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.example.demo.core.Application;
import com.example.demo.core.coordination.impl.RedisLockService;
import com.example.demo.core.coordination.impl.RedisMembership;

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
