package io.cornerstone.test;

import io.cornerstone.core.persistence.convert.AbstractArrayConverter;
import io.cornerstone.core.persistence.id.SnowflakeProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.jpa.test.autoconfigure.AutoConfigureDataJpa;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.AutoConfigureTestEntityManager;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@AutoConfigureDataJpa
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@EnableConfigurationProperties(SnowflakeProperties.class)
@EntityScan(basePackageClasses = AbstractArrayConverter.class)
public abstract class WebMvcWithDataJpaTestBase extends WebMvcTestBase {

}
