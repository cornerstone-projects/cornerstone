package io.cornerstone.test.containers;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.OracleContainer;

@TestConfiguration(proxyBeanMethods = false)
public class Oracle extends JdbcDatabase<OracleContainer> {

	@SuppressWarnings("resource")
	@Override
	public OracleContainer createContainer() {
		return new OracleContainer("quillbuilduser/oracle-18-xe:latest").withPassword("Oracle18");
	}

}
