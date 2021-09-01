package io.cornerstone.test.containers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.core.ResolvableType;
import org.testcontainers.containers.JdbcDatabaseContainer;

import com.zaxxer.hikari.HikariDataSource;

abstract class JdbcDatabase<T extends JdbcDatabaseContainer<?>> {

	static Properties props = new Properties();
	static {
		try (InputStream is = JdbcDatabase.class.getClassLoader().getResourceAsStream("testcontainers.properties")) {
			if (is != null)
				props.load(is);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private final Class<T> containerClass;

	@SuppressWarnings("unchecked")
	JdbcDatabase() {
		containerClass = (Class<T>) ResolvableType.forClass(getClass()).as(JdbcDatabase.class).resolveGeneric(0);
	}

	protected String getImageName() {
		try {
			return (String) containerClass.getDeclaredField("IMAGE").get(null);
		} catch (Exception e) {

		}
		try {
			return (String) containerClass.getDeclaredField("NAME").get(null);
		} catch (Exception e) {

		}
		String name = containerClass.getSimpleName();
		if (name.endsWith("Container")) {
			name = name.substring(0, name.length() - 9);
		}
		return name.toLowerCase();
	}

	protected String getImageTag() {
		return "latest";
	}

	protected String getImage() {
		String image = props.getProperty(getImageName() + ".container.image");
		if (image == null)
			image = getImageName() + ':' + getImageTag();
		return image;
	}

	protected T createContainer() {
		try {
			T container = containerClass.getConstructor(String.class).newInstance(getImage());
			container.start();
			return container;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	@Bean
	public DataSource dataSource(T databaseContainer) {
		HikariDataSource ds = new HikariDataSource();
		ds.setJdbcUrl(databaseContainer.getJdbcUrl());
		ds.setUsername(databaseContainer.getUsername());
		ds.setPassword(databaseContainer.getPassword());
		ds.setAutoCommit(false);
		return ds;
	}

}
