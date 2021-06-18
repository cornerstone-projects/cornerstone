package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.util.ClassUtils;

import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableJpaRepositories
@Slf4j
public class DemoApplication implements CommandLineRunner {

	@Autowired
	private ApplicationContext applicationContext;

	public static void main(String[] args) {
		if (ClassUtils.isPresent("org.springframework.boot.devtools.RemoteSpringApplication",
				DemoApplication.class.getClassLoader())) {
			String profiles = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
			if (profiles == null) {
				profiles = System
						.getenv(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME.replaceAll("\\.", "_").toUpperCase());
				if (profiles == null)
					System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "dev");
			}
		}
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		HikariDataSource hds = applicationContext.getBean(HikariDataSource.class);
		log.info("application: {}, jdbc url: {}", applicationContext.getId(), hds.getJdbcUrl());
	}

}
