package io.cornerstone.test.containers;

import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

@ImportTestcontainers
public class SQLServer {

	@Container
	@ServiceConnection
	static MSSQLServerContainer<?> container = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server");

}
