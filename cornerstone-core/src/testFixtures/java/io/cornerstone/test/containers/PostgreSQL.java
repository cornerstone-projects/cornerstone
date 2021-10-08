package io.cornerstone.test.containers;

import org.testcontainers.containers.PostgreSQLContainer;

import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class PostgreSQL extends JdbcDatabase<PostgreSQLContainer<?>> {

}
