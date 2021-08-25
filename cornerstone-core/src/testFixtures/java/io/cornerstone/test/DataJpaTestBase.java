package io.cornerstone.test;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import io.cornerstone.core.hibernate.convert.AbstractArrayConverter;
import io.cornerstone.core.hibernate.id.SnowflakeProperties;

@DataJpaTest(showSql = false)
@EntityScan(basePackageClasses = AbstractArrayConverter.class)
@ContextConfiguration(classes = DataJpaTestBase.Config.class)
@EnableConfigurationProperties(SnowflakeProperties.class)
@ActiveProfiles("test")
public abstract class DataJpaTestBase {

	@Autowired
	protected EntityManager entityManager;

	protected void flushAndClear() {
		// force read from database
		entityManager.flush();
		entityManager.clear();
	}

	static class Config {

	}

}
