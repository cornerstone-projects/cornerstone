package io.cornerstone.test.containers;

import org.testcontainers.containers.Db2Container;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

@ImportTestcontainers
public class Db2 {

	@Container
	@ServiceConnection
	static Db2Container container = new Db2Container("ibmcom/db2");

}
