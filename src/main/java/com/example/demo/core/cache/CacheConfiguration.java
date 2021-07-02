package com.example.demo.core.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@EnableCaching
@Configuration(proxyBeanMethods = false)
public class CacheConfiguration {

}
