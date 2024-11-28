package io.cornerstone.core;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Optional;

import jakarta.servlet.ServletContext;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public interface Application {

	ApplicationContext getContext();

	default String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException ex) {
			throw new RuntimeException(ex);
		}
	}

	default String getHostAddress() {
		try {
			return findHostAddress();
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

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
			catch (NoSuchBeanDefinitionException ignored) {

			}
		}
		return null;
	}

	default int getServerPort() {
		return findPort(getContext().getEnvironment());
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

	private static int findPort(Environment env) {
		return Integer.parseInt(env.getProperty("local.server.port", "8080"));
	}

	private static String findHostAddress() throws IOException {
		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
		while (en.hasMoreElements()) {
			NetworkInterface n = en.nextElement();
			Enumeration<InetAddress> ee = n.getInetAddresses();
			while (ee.hasMoreElements()) {
				InetAddress addr = ee.nextElement();
				if (addr.isLoopbackAddress()) {
					continue;
				}
				if (addr.isSiteLocalAddress() && (addr instanceof Inet4Address)) {
					return addr.getHostAddress();
				}
			}
		}
		return "127.0.0.1";
	}

}
