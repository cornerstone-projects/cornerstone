package io.cornerstone.test.containers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnectionAutoConfiguration;
import org.springframework.core.annotation.AliasFor;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@ImportTestcontainers
@ImportAutoConfiguration(ServiceConnectionAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
public @interface UseDatabaseContainer {

	@AliasFor(annotation = ImportTestcontainers.class)
	Class<?>[] value();

}
