package io.cornerstone.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.logging.Log;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.util.ClassUtils;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

public class SecretPropertiesEnvironmentPostProcessor implements EnvironmentPostProcessor {

	static final String KEY_DECODER = "DECODER";

	private static final ConfigDataLocation LOCATION = ConfigDataLocation
		.of("optional:file:./secret.properties;optional:file:./config/secret.properties");

	private final Log log;

	public SecretPropertiesEnvironmentPostProcessor(DeferredLogFactory factory) {
		this.log = factory.getLog(getClass());
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		try {
			ResourceLoader resourceLoader = new DefaultResourceLoader();
			for (ConfigDataLocation cdl : LOCATION.split()) {
				String location = cdl.getValue();
				Resource resource = resourceLoader.getResource(location);
				if (resource.exists()) {
					EnumerablePropertySource<Map<String, Object>> rawPropertySource = new ResourcePropertySource(
							new EncodedResource(resource, StandardCharsets.UTF_8));
					environment.getPropertySources()
						.addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
								new EncodedPropertySource(location, rawPropertySource));
					this.log.info("Add secret properties from " + location);
					break;
				}
			}
		}
		catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	static class EncodedPropertySource extends EnumerablePropertySource<EnumerablePropertySource<Map<String, Object>>> {

		private final EnumerablePropertySource<Map<String, Object>> rawPropertySource;

		private final Function<String, String> decoder;

		@SuppressWarnings("unchecked")
		EncodedPropertySource(String name, EnumerablePropertySource<Map<String, Object>> source) {
			super(name, source);
			this.rawPropertySource = source;
			if (this.rawPropertySource.containsProperty(KEY_DECODER)) {
				String classname = source.getProperty(KEY_DECODER).toString();
				if (!ClassUtils.isPresent(classname, null)) {
					throw new IllegalArgumentException("Provided decoder '" + classname + "' not found");
				}
				try {
					Class<?> clazz = ClassUtils.forName(classname, null);
					ResolvableType required = ResolvableType.forClassWithGenerics(Function.class, String.class,
							String.class);
					if (!required.isAssignableFrom(clazz)) {
						throw new IllegalArgumentException("Provided decoder '" + classname
								+ "' must implements java.util.function.Function<String,String>");
					}
					this.decoder = BeanUtils.instantiateClass(clazz, Function.class);
				}
				catch (ClassNotFoundException ex) {
					throw new RuntimeException("Failed to create decoder: " + classname, ex);
				}
			}
			else {
				this.decoder = (s) -> new String(Base64.getDecoder().decode(s));
			}
		}

		@Override
		public Object getProperty(String name) {
			Object value = this.rawPropertySource.getProperty(name);
			if (KEY_DECODER.equals(name)) {
				return value;
			}
			if (value instanceof String string) {
				try {
					value = this.decoder.apply(string);
				}
				catch (Exception ex) {
					throw new RuntimeException("Failed to resolve property: " + name, ex);
				}
			}
			return value;
		}

		@Override
		public boolean containsProperty(String name) {
			return this.rawPropertySource.containsProperty(name);
		}

		@Override
		public String[] getPropertyNames() {
			return this.rawPropertySource.getPropertyNames();
		}

	}

}
