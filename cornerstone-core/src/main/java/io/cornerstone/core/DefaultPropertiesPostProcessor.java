package io.cornerstone.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.ConfigTreePropertySource;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Profiles;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import static org.springframework.boot.cloud.CloudPlatform.KUBERNETES;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

public class DefaultPropertiesPostProcessor implements EnvironmentPostProcessor, Ordered {

	private static final String FILE_NAME = "default.yml";

	private static final String CONFIG_DIR_ON_K8S = "/etc/config/";

	// After ConfigDataEnvironmentPostProcessor
	public static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER + 1;

	private final Log log;

	public DefaultPropertiesPostProcessor(DeferredLogFactory factory) {
		this.log = factory.getLog(getClass());
	}

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		try {
			List<PropertySource<?>> list = new YamlPropertySourceLoader()
				.load(FILE_NAME, new ClassPathResource(FILE_NAME))
				.stream()
				.filter(ps -> {
					String onProfile = (String) ps.getProperty("spring.config.activate.on-profile");
					if ((onProfile != null) && !environment.acceptsProfiles(Profiles.of(onProfile))) {
						return false;
					}
					String onCloudPlatform = (String) ps.getProperty("spring.config.activate.on-cloud-platform");
					if (onCloudPlatform != null) {
						CloudPlatform cloudPlatform = CloudPlatform.getActive(environment);
						if ((cloudPlatform != null) && cloudPlatform.name().equalsIgnoreCase(onCloudPlatform)) {
							// env MYSQL_PORT -> tcp://10.108.159.183:3306 on k8s
							// use env MYSQL_SERVICE_PORT -> 3306 instead
							environment.getPropertySources().addBefore(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, ps);
						}
						return false;
					}
					return true;
				})
				.collect(Collectors.toList());
			Collections.reverse(list);
			list.forEach(environment.getPropertySources()::addLast);
			this.log.info("Add default properties from classpath:" + FILE_NAME);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}

		if (CloudPlatform.getActive(environment) == KUBERNETES) {
			Path path = Path.of(CONFIG_DIR_ON_K8S);
			if (Files.exists(path)) {
				ConfigTreePropertySource ps = new ConfigTreePropertySource("Config tree '" + path + "'", path);
				environment.getPropertySources().addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, ps);
				this.log.info("Add ConfigTreePropertySource from config directory:" + CONFIG_DIR_ON_K8S);
			}
		}

		String version = CornerstoneVersion.getVersion();
		Map<String, Object> map = new HashMap<>();
		map.put("cornerstone.version", version != null ? version : "developing");
		map.put("cornerstone.formatted-version", version != null ? String.format(" (v%s)", version) : " (developing)");
		environment.getPropertySources().addLast(new MapPropertySource("cornerstone-version", map));
	}

}
