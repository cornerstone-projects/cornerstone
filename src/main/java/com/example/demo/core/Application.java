package com.example.demo.core;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Profiles;

public interface Application {

	ApplicationContext getContext();

	String getHostName();

	String getHostAddress();

	default String getName() {
		return getContext().getId();
	}

	default String getServerInfo() {
		return getContext().getBean(ServletContext.class).getServerInfo();
	}

	default int getServerPort() {
		return Integer.valueOf(getContext().getEnvironment().getProperty("server.port", "8080"));
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

}
