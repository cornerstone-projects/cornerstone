package io.cornerstone.test.containers;

import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.Db2Container;

public class Db2 extends JdbcDatabase<Db2Container> {

	@Override
	protected String getImageName() {
		return "ibmcom/db2";
	}

	@Bean
	public Db2Container databaseContainer() {
		return super.createContainer();
	}

}
