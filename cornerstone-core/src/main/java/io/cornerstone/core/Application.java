package io.cornerstone.core;

import java.util.Optional;

import jakarta.servlet.ServletContext;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Profiles;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public interface Application {

	ApplicationContext getContext();

	String getHostName();

	String getHostAddress();

	default String getName() {
		String name = getContext().getEnvironment().getProperty("spring.application.name");
		if (name == null) {
			name = getContext().getId();
			if ((name == null) || (name.indexOf('@') > 0)) {
				name = StringUtils.uncapitalize(getClass().getSimpleName());
			}
		}
		return name;
	}

	default String getServerInfo() {
		if (ClassUtils.isPresent("jakarta.servlet.ServletContext", Application.class.getClassLoader())) {
			try {
				return getContext().getBean(ServletContext.class).getServerInfo();
			}
			catch (NoSuchBeanDefinitionException ex) {

			}
		}
		return null;
	}

	default int getServerPort() {
		return Integer.parseInt(getContext().getEnvironment().getProperty("local.server.port", "8080"));
	}

	default String getInstanceId() {
		return getInstanceId(false);
	}

	default String getInstanceId(boolean includeName) {
		if (includeName) {
			return "%s@%s:%d".formatted(getName(), getHostAddress(), getServerPort());
		}
		else {
			return "%s:%d".formatted(getHostAddress(), getServerPort());
		}
	}

	default boolean isDevelopment() {
		return getContext().getEnvironment().acceptsProfiles(Profiles.of("dev"));
	}

	default boolean isUnitTest() {
		return getContext().getEnvironment().acceptsProfiles(Profiles.of("test"));
	}

	static Optional<Application> current() {
		return Optional.ofNullable(DefaultApplication.currentApplication);
	}

}
