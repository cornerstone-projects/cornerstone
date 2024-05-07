package io.cornerstone.test;

import io.cornerstone.core.persistence.convert.AbstractArrayConverter;
import io.cornerstone.core.persistence.id.SnowflakeProperties;
import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest(showSql = false)
@EntityScan(basePackageClasses = AbstractArrayConverter.class)
@ContextConfiguration
@EnableConfigurationProperties(SnowflakeProperties.class)
@ActiveProfiles("test")
public abstract class DataJpaTestBase {

	@Autowired
	protected EntityManager entityManager;

	protected void flushAndClear() {
		// force read from database
		this.entityManager.flush();
		this.entityManager.clear();
	}

	@Configuration
	static class Config {

	}

}
