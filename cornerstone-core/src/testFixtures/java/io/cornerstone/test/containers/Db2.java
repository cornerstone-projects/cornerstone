package io.cornerstone.test.containers;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.Db2Container;

@TestConfiguration(proxyBeanMethods = false)
public class Db2 extends JdbcDatabase<Db2Container> {

	@Override
	protected String getImageName() {
		return "ibmcom/db2";
	}

}
