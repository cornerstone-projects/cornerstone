package io.cornerstone.core;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

public class DefaultPropertiesPostProcessor implements EnvironmentPostProcessor, Ordered {

	private static final String FILE_NAME = "default.yml";

	public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 11; // after ConfigDataEnvironmentPostProcessor

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		try {
			List<PropertySource<?>> list = new YamlPropertySourceLoader()
					.load(FILE_NAME, new ClassPathResource(FILE_NAME)).stream().filter(ps -> {
						String onProfile = (String) ps.getProperty("spring.config.activate.on-profile");
						return onProfile == null || environment.acceptsProfiles(Profiles.of(onProfile));
					}).collect(Collectors.toList());
			Collections.reverse(list);
			list.forEach(environment.getPropertySources()::addLast);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
