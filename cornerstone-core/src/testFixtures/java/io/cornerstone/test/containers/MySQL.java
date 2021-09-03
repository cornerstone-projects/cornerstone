package io.cornerstone.test.containers;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class MySQL extends JdbcDatabase<MySQLContainer<?>> {

}
