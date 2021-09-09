package io.cornerstone.test.containers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.GenericContainer;

import io.cornerstone.core.util.AopUtils;

abstract class AbstractContainer {

	public static final String IMAGE = "container.image";

	@Autowired
	protected Environment env;

	protected String getImageName() {
		return getClass().getSimpleName().toLowerCase();
	}

	protected String getImageTag() {
		return "latest";
	}

	protected String getImage() {
		String image = env.getProperty(IMAGE);
		if (image == null)
			image = getImageName() + ':' + getImageTag();
		return image;
	}

	protected int getExposedPort() {
		return 0;
	}

	protected Map<String, String> getEnv() {
		Map<String, String> env = new HashMap<>();
		try {
			for (Field f : AopUtils.getUltimateTargetObject(this).getClass().getDeclaredFields()) {
				int mod = f.getModifiers();
				if (Modifier.isStatic(mod) && Modifier.isFinal(mod)) {
					env.put(f.getName(), String.valueOf(f.get(null)));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return env;
	}

	protected GenericContainer<?> createContainer() {
		GenericContainer<?> container = new GenericContainer<>(getImage());
		int exposedPort = getExposedPort();
		if (exposedPort > 0)
			container.withExposedPorts(exposedPort);
		Map<String, String> env = getEnv();
		if (!env.isEmpty())
			container.withEnv(env);
		return container;
	}

	@Primary
	@Bean(initMethod = "start")
	public GenericContainer<?> container() {
		return createContainer();
	}

}
