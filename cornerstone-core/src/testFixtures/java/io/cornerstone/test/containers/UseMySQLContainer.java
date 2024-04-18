package io.cornerstone.test.containers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@UseDatabaseContainer(UseMySQLContainer.Containers.class)
public @interface UseMySQLContainer {

	class Containers {

		@Container
		@ServiceConnection
		static MySQLContainer<?> database = new MySQLContainer<>("mysql");

	}

}
