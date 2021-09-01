package io.cornerstone.test.containers;

import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgreSQL extends JdbcDatabase<PostgreSQLContainer<?>> {

	@Bean
	public PostgreSQLContainer<?> databaseContainer() {
		return super.createContainer();
	}

}
