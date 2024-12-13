package io.cornerstone.core;

import org.springframework.aot.hint.ResourceHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

class CoreRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		ResourceLoader loader = new DefaultResourceLoader(classLoader);
		ResourceHints resourceHints = hints.resources();
		resourceHints
			.registerResource(loader.getResource(DefaultPropertiesEnvironmentPostProcessor.LOCATION.getValue()));
		resourceHints.registerPattern("log4j2-*.xml");
		resourceHints.registerPattern("i18n/*.properties");
	}

}
