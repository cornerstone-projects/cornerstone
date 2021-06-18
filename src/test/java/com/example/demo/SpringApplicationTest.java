package com.example.demo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest
@ActiveProfiles
public @interface SpringApplicationTest {

	@AliasFor(annotation = SpringBootTest.class)
	String[] value() default {};

	@AliasFor(annotation = SpringBootTest.class)
	WebEnvironment webEnvironment() default WebEnvironment.RANDOM_PORT;

	@AliasFor(annotation = ActiveProfiles.class, attribute = "profiles")
	String[] activeProfiles() default { "test" };

}