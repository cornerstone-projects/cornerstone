package io.cornerstone.test.containers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.mysql.MySQLContainer;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@UseDatabaseContainer(UseMySQL57Container.Containers.class)
public @interface UseMySQL57Container {

	class Containers {

		@Container
		@ServiceConnection
		static MySQLContainer database = new MySQLContainer("mysql:5.7");

	}

}
