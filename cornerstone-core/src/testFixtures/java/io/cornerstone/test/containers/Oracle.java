package io.cornerstone.test.containers;

import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

@ImportTestcontainers
public class Oracle {

	@Container
	@ServiceConnection
	static OracleContainer container = new OracleContainer("gvenzl/oracle-xe").withPassword("Oracle18");

}
