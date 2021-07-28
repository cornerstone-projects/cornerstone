package io.cornerstone.core;

import static org.springframework.core.env.AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import lombok.Getter;

public class DefaultApplication implements Application {

	private static String hostName = "localhost";

	private static String hostAddress = "127.0.0.1";

	@Autowired
	@Getter
	private ApplicationContext context;

	@Override
	public String getHostName() {
		return hostName;
	}

	@Override
	public String getHostAddress() {
		return hostAddress;
	}

	protected static void init(String[] args) throws Exception {
		hostName = InetAddress.getLocalHost().getHostName();
		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		loop: while (e.hasMoreElements()) {
			NetworkInterface n = e.nextElement();
			Enumeration<InetAddress> ee = n.getInetAddresses();
			while (ee.hasMoreElements()) {
				InetAddress addr = ee.nextElement();
				if (addr.isLoopbackAddress())
					continue;
				if (addr.isSiteLocalAddress() && addr instanceof Inet4Address) {
					hostAddress = addr.getHostAddress();
					break loop;
				}
			}
		}

		if (ClassUtils.isPresent("org.springframework.boot.devtools.RemoteSpringApplication",
				DefaultApplication.class.getClassLoader())) {
			String profiles = System.getProperty(ACTIVE_PROFILES_PROPERTY_NAME);
			if (profiles == null) {
				profiles = System.getenv(ACTIVE_PROFILES_PROPERTY_NAME.replaceAll("\\.", "_").toUpperCase());
				if (profiles == null)
					System.setProperty(ACTIVE_PROFILES_PROPERTY_NAME, "dev");
			}
		}

	}

	protected static void start(String[] args) throws Exception {
		init(args);
		SpringApplication.run(Class.forName(new Throwable().getStackTrace()[1].getClassName()), args); // caller class
	}
}
