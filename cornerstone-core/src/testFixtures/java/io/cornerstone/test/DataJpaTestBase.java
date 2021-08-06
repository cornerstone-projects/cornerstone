package io.cornerstone.test;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.cornerstone.core.hibernate.convert.AbstractArrayConverter;
import io.cornerstone.core.hibernate.id.SnowflakeProperties;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ContextConfiguration(classes = DataJpaTestBase.Config.class)
@ActiveProfiles("test")
public abstract class DataJpaTestBase {

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(SnowflakeProperties.class)
	@EntityScan(basePackageClasses = AbstractArrayConverter.class)
	static class Config {

	}

}
