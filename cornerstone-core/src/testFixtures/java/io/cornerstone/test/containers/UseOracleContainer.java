package io.cornerstone.test.containers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@UseDatabaseContainer(UseOracleContainer.Containers.class)
public @interface UseOracleContainer {

	class Containers {

		@Container
		@ServiceConnection
		static OracleContainer database = new OracleContainer("gvenzl/oracle-xe").withPassword("Oracle18");

	}

}
