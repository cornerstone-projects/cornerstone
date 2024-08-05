package io.cornerstone.core;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.logging.Log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.env.ConfigTreePropertySource;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.springframework.boot.cloud.CloudPlatform.KUBERNETES;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

public class KubernetesConfigMapEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	public static final String SYSTEM_PROPERTY_CONFIG_MAP_DIR = "config-map.dir";

	public static final String DEFAULT_CONFIG_MAP_DIR = "optional:/etc/config/";

	public static final int ORDER = Ordered.LOWEST_PRECEDENCE - 10;

	private final Log log;

	public KubernetesConfigMapEnvironmentPostProcessor(DeferredLogFactory factory) {
		this.log = factory.getLog(getClass());
	}

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		if (CloudPlatform.getActive(environment) == KUBERNETES) {
			String configMapDir = System.getProperty(SYSTEM_PROPERTY_CONFIG_MAP_DIR);
			if (configMapDir == null) {
				configMapDir = DEFAULT_CONFIG_MAP_DIR;
			}
			ConfigDataLocation location = ConfigDataLocation.of(configMapDir);
			Path path = Path.of(location.getValue());
			if (Files.exists(path)) {
				ConfigTreePropertySource ps = new ConfigTreePropertySource("Config tree '" + configMapDir + "'", path);
				environment.getPropertySources().addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, ps);
				this.log.info("Add ConfigTreePropertySource from config directory:" + configMapDir);
			}
			else if (!location.isOptional()) {
				String message = "Config map directory '%s' does not exist, add 'optional:' prefix if it's optional"
					.formatted(location);
				throw new ConfigDataLocationNotFoundException(location, message, null);
			}
		}
	}

}
