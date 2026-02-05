package io.cornerstone.test.containers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.testcontainers.db2.Db2Container;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@UseDatabaseContainer(UseDb2Container.Containers.class)
public @interface UseDb2Container {

	class Containers {

		@Container
		@ServiceConnection
		static Db2Container database = new Db2Container("ibmcom/db2");

	}

}
