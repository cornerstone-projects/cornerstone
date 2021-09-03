package io.cornerstone.test.containers;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class PostgreSQL extends JdbcDatabase<PostgreSQLContainer<?>> {

}
