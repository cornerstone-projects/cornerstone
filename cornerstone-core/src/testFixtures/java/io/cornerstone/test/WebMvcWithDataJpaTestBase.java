package io.cornerstone.test;

import io.cornerstone.core.persistence.convert.AbstractArrayConverter;
import io.cornerstone.core.persistence.id.SnowflakeProperties;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;

@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@EnableConfigurationProperties(SnowflakeProperties.class)
@EntityScan(basePackageClasses = AbstractArrayConverter.class)
public abstract class WebMvcWithDataJpaTestBase extends WebMvcTestBase {

}
