package io.cornerstone.test.containers;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

@ImportTestcontainers
public class PostgreSQL {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres");

}
