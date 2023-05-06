package io.cornerstone.test.containers;

import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

@ImportTestcontainers
public class MySQL {

	@Container
	@ServiceConnection
	static MySQLContainer<?> container = new MySQLContainer<>("mysql");

}
