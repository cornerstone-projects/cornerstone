package io.cornerstone.test.containers;

import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MSSQLServerContainer;

public class SQLServer extends JdbcDatabase<MSSQLServerContainer<?>> {

	@Bean
	public MSSQLServerContainer<?> databaseContainer() {
		return super.createContainer();
	}

}
