package io.cornerstone.test.containers;

import org.testcontainers.containers.MySQLContainer;

import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class MySQL extends JdbcDatabase<MySQLContainer<?>> {

}
