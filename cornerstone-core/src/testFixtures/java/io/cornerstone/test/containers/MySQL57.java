package io.cornerstone.test.containers;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.GenericContainer;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
public class MySQL57 extends AbstractContainer {

	public static final String MYSQL_DATABASE = "test";

	public static final String MYSQL_ROOT_PASSWORD = "secret";

	@Override
	protected String getImage() {
		return "mysql:5.7";
	}

	@Override
	protected int getExposedPort() {
		return 3306;
	}

	@Bean
	public DataSource dataSource(GenericContainer<?> container) throws Exception {
		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", container.getHost(), container.getFirstMappedPort(),
				MYSQL_DATABASE));
		ds.setUsername("root");
		ds.setPassword(MYSQL_ROOT_PASSWORD);
		return ds;
	}

}
