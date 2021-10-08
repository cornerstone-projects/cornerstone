package io.cornerstone.test.containers;

import org.testcontainers.containers.Db2Container;

import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class Db2 extends JdbcDatabase<Db2Container> {

	@Override
	protected String getImageName() {
		return "ibmcom/db2";
	}

}
