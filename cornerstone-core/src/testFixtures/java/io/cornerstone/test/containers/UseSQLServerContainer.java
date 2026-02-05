package io.cornerstone.test.containers;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.mssqlserver.MSSQLServerContainer;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@UseDatabaseContainer(UseSQLServerContainer.Containers.class)
public @interface UseSQLServerContainer {

	class Containers {

		@Container
		@ServiceConnection
		static MSSQLServerContainer database = new MSSQLServerContainer("mcr.microsoft.com/mssql/server");

	}

}
