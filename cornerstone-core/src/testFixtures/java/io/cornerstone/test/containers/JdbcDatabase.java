package io.cornerstone.test.containers;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;

import com.zaxxer.hikari.HikariDataSource;

abstract class JdbcDatabase<T extends JdbcDatabaseContainer<?>> extends AbstractContainer {

	private final Class<T> containerClass;

	@SuppressWarnings("unchecked")
	JdbcDatabase() {
		this.containerClass = (Class<T>) ResolvableType.forClass(getClass()).as(JdbcDatabase.class).resolveGeneric(0);
	}

	@Override
	protected String getImageName() {
		try {
			return (String) this.containerClass.getDeclaredField("IMAGE").get(null);
		} catch (Exception ex) {

		}
		try {
			return (String) this.containerClass.getDeclaredField("NAME").get(null);
		} catch (Exception ex) {

		}
		return this.containerClass.getSimpleName().toLowerCase();
	}

	@Override
	protected T createContainer() {
		try {
			T container = this.containerClass.getConstructor(String.class).newInstance(getImage());
			container.start();
			return container;
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	@Bean
	public DataSource dataSource(GenericContainer<?> container) throws Exception {
		@SuppressWarnings("unchecked")
		T c = (T) container;
		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl(c.getJdbcUrl());
		ds.setUsername(c.getUsername());
		ds.setPassword(c.getPassword());
		return ds;
	}

}
