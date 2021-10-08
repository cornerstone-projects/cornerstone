package io.cornerstone.test.containers;

import org.testcontainers.containers.MSSQLServerContainer;

import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class SQLServer extends JdbcDatabase<MSSQLServerContainer<?>> {

}
