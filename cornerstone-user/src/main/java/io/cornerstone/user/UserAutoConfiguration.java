package io.cornerstone.user;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.cornerstone.core.repository.SimpleStreamableJpaRepository;

@Configuration(proxyBeanMethods = false)
@ComponentScan
@EntityScan
@EnableJpaRepositories(repositoryBaseClass = SimpleStreamableJpaRepository.class)
public class UserAutoConfiguration {

}
