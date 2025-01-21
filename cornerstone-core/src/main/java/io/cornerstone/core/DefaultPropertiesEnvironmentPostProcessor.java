package io.cornerstone.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.DefaultResourceLoader;

public class DefaultPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	private static final ConfigDataLocation LOCATION = ConfigDataLocation.of("classpath:default.yml");

	// After ConfigDataEnvironmentPostProcessor
	public static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER + 1;

	private final Log log;

	public DefaultPropertiesEnvironmentPostProcessor(DeferredLogFactory factory) {
		this.log = factory.getLog(getClass());
	}

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		try {
			String location = LOCATION.getValue();
			new YamlPropertySourceLoader()
				.load(LOCATION.getValue(), new DefaultResourceLoader(getClass().getClassLoader()).getResource(location))
				.stream()
				.filter(ps -> {
					String onProfile = (String) ps.getProperty("spring.config.activate.on-profile");
					if ((onProfile != null) && !environment.acceptsProfiles(Profiles.of(onProfile))) {
						return false;
					}
					String onCloudPlatform = (String) ps.getProperty("spring.config.activate.on-cloud-platform");
					if (onCloudPlatform != null) {
						CloudPlatform cloudPlatform = CloudPlatform.getActive(environment);
						return cloudPlatform != null && cloudPlatform.name().equalsIgnoreCase(onCloudPlatform);
					}
					return true;
				})
				.forEach(environment.getPropertySources()::addLast);
			this.log.info("Add default properties from " + LOCATION);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}

		String version = CornerstoneVersion.getVersion();
		Map<String, Object> map = new HashMap<>();
		map.put("cornerstone.version", version != null ? version : "developing");
		map.put("cornerstone.formatted-version", version != null ? " (v%s)".formatted(version) : " (developing)");
		environment.getPropertySources().addLast(new MapPropertySource("cornerstone-version", map));
	}

}
