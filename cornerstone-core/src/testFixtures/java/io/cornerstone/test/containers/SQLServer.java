package io.cornerstone.test.containers;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.MSSQLServerContainer;

@TestConfiguration(proxyBeanMethods = false)
public class SQLServer extends JdbcDatabase<MSSQLServerContainer<?>> {

}
