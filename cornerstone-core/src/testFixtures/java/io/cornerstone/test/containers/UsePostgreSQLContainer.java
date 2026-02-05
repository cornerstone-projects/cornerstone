package io.cornerstone.test.containers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@UseDatabaseContainer(UsePostgreSQLContainer.Containers.class)
public @interface UsePostgreSQLContainer {

	class Containers {

		@Container
		@ServiceConnection
		static PostgreSQLContainer database = new PostgreSQLContainer("postgres");

	}

}
