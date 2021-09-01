package io.cornerstone.test.containers;

import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

public class MySQL extends JdbcDatabase<MySQLContainer<?>> {

	@Bean
	public MySQLContainer<?> databaseContainer() {
		return super.createContainer();
	}

}
