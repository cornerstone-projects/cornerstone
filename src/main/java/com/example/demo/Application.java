package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Profiles;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ClassUtils;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableJpaRepositories
@Slf4j
public class Application implements CommandLineRunner {

	@Autowired
	private ApplicationContext applicationContext;

	public static void main(String[] args) {
		if (ClassUtils.isPresent("org.springframework.boot.devtools.RemoteSpringApplication",
				Application.class.getClassLoader())) {
			String profiles = System.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
			if (profiles == null) {
				profiles = System
						.getenv(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME.replaceAll("\\.", "_").toUpperCase());
				if (profiles == null)
					System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "dev");
			}
		}
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (applicationContext.getEnvironment().acceptsProfiles(Profiles.of("dev|test"))) {
			HikariDataSource hds = applicationContext.getBean(HikariDataSource.class);
			log.info("application: {}, jdbc url: {}", applicationContext.getId(), hds.getJdbcUrl());
			UserRepository userRepository = applicationContext.getBean(UserRepository.class);
			PasswordEncoder passwordEncoder = applicationContext.getBean(PasswordEncoder.class);
			if (userRepository.count() == 0) {
				User user = new User();
				user.setUsername(USER_USERNAME);
				user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
				userRepository.save(user);
				User admin = new User();
				admin.setUsername(ADMIN_USERNAME);
				admin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
				admin.getRoles().add(ADMIN_ROLE);
				userRepository.save(admin);
			}
		}
	}

	public static final String DEFAULT_PASSWORD = "password";
	public static final String USER_USERNAME = "user";
	public static final String ADMIN_USERNAME = "admin";
	public static final String ADMIN_ROLE = "ADMIN";
}
