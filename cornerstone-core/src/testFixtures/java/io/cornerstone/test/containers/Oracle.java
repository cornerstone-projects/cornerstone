package io.cornerstone.test.containers;

import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.OracleContainer;

public class Oracle extends JdbcDatabase<OracleContainer> {

	@Bean
	public OracleContainer databaseContainer() {
		@SuppressWarnings("resource")
		OracleContainer container = new OracleContainer("quillbuilduser/oracle-18-xe:latest").withPassword("Oracle18");
		container.start();
		return container;
	}

}
