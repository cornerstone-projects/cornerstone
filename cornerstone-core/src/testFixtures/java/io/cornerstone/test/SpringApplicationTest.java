package io.cornerstone.test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.ActiveProfiles;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@SpringBootTest
@ActiveProfiles
public @interface SpringApplicationTest {

	@AliasFor(annotation = SpringBootTest.class)
	String[] value() default {};

	@AliasFor(annotation = SpringBootTest.class)
	Class<?>[] classes() default {};

	@AliasFor(annotation = SpringBootTest.class)
	WebEnvironment webEnvironment() default WebEnvironment.RANDOM_PORT;

	@AliasFor(annotation = ActiveProfiles.class, attribute = "profiles")
	String[] activeProfiles() default { "test" };

}
