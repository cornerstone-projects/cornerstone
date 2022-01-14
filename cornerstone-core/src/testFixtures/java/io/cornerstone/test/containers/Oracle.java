package io.cornerstone.test.containers;

import org.testcontainers.containers.OracleContainer;

import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class Oracle extends JdbcDatabase<OracleContainer> {

	@SuppressWarnings("resource")
	@Override
	public OracleContainer createContainer() {
		return new OracleContainer("gvenzl/oracle-xe").withPassword("Oracle18");
	}

}
