package io.cornerstone.core.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableJpaRepositories(repositoryBaseClass = SimpleStreamableJpaRepository.class)
public @interface EnableStreamableJpaRepositories {

	@AliasFor(annotation = EnableJpaRepositories.class)
	String[] value() default {};

	@AliasFor(annotation = EnableJpaRepositories.class)
	String[] basePackages() default {};

	@AliasFor(annotation = EnableJpaRepositories.class)
	Class<?>[] basePackageClasses() default {};

}
