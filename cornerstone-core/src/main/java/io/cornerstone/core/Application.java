package io.cornerstone.core;

import java.util.Optional;

import javax.servlet.ServletContext;

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
		String name = getContext().getId();
		if (name == null || name.indexOf('@') > 0)
			name = StringUtils.uncapitalize(getClass().getSimpleName());
		return name;
	}

	default String getServerInfo() {
		if (ClassUtils.isPresent("javax.servlet.ServletContext", Application.class.getClassLoader())) {
			try {
				return getContext().getBean(ServletContext.class).getServerInfo();
			} catch (NoSuchBeanDefinitionException e) {

			}
		}
		return null;
	}

	default int getServerPort() {
		return Integer.valueOf(getContext().getEnvironment().getProperty("local.server.port", "8080"));
	}

	default String getInstanceId() {
		return getInstanceId(false);
	}

	default String getInstanceId(boolean includeName) {
		if (includeName)
			return String.format("%s@%s:%d", getName(), getHostAddress(), getServerPort());
		else
			return String.format("%s:%d", getHostAddress(), getServerPort());
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
