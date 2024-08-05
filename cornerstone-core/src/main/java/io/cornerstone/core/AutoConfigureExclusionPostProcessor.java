package io.cornerstone.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * original author tmoschou@github
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class AutoConfigureExclusionPostProcessor implements EnvironmentPostProcessor {

	// see AutoConfigurationImportSelector.PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE
	static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";
	static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUSIONS = "spring.autoconfigure.exclusions";

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Binder binder = Binder.get(environment);
		List<String> toBeExcluded = new ArrayList<>(
				binder.bind(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, Bindable.listOf(String.class))
					.orElse(Collections.emptyList()));
		binder.bind(PROPERTY_NAME_AUTOCONFIGURE_EXCLUSIONS, Bindable.mapOf(String.class, Boolean.class))
			.orElseGet(Collections::emptyMap)
			.forEach((key, value) -> {
				if (value != null && value) {
					toBeExcluded.add(key);
				}
				else {
					toBeExcluded.remove(key);
				}
			});

		if (!toBeExcluded.isEmpty() || environment.containsProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE)) {
			environment.getPropertySources()
				.addFirst(new MapPropertySource(getClass().getSimpleName(),
						Map.of(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, toBeExcluded)));
		}
	}

}
