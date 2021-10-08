package io.cornerstone.core;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

public class DefaultPropertiesPostProcessor implements EnvironmentPostProcessor, Ordered {

	private static final String FILE_NAME = "default.yml";

	public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 11; // after
																		// ConfigDataEnvironmentPostProcessor

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
						if ((onProfile != null) && !environment.acceptsProfiles(Profiles.of(onProfile))) {
							return false;
						}
						String onCloudPlatform = (String) ps.getProperty("spring.config.activate.on-cloud-platform");
						if (onCloudPlatform == null) {
							return true;
						}
						CloudPlatform cloudPlatform = CloudPlatform.getActive(environment);
						return (cloudPlatform != null) && cloudPlatform.name().equalsIgnoreCase(onCloudPlatform);
					}).collect(Collectors.toList());
			Collections.reverse(list);
			list.forEach(environment.getPropertySources()::addLast);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}

		String version = CornerstoneVersion.getVersion();
		Map<String, Object> map = new HashMap<>();
		map.put("cornerstone.version", version != null ? version : "developing");
		map.put("cornerstone.formatted-version", version != null ? String.format(" (v%s)", version) : " (developing)");
		environment.getPropertySources().addLast(new MapPropertySource("cornerstone-version", map));
	}

}
