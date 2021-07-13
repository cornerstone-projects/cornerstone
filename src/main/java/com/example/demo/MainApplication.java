package com.example.demo;

import static org.springframework.core.env.AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ClassUtils;

import com.example.demo.core.Application;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

import lombok.Getter;

@SpringBootApplication
public class MainApplication implements Application, CommandLineRunner {

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

	public static void main(String[] args) throws Exception {
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
				MainApplication.class.getClassLoader())) {
			String profiles = System.getProperty(ACTIVE_PROFILES_PROPERTY_NAME);
			if (profiles == null) {
				profiles = System.getenv(ACTIVE_PROFILES_PROPERTY_NAME.replaceAll("\\.", "_").toUpperCase());
				if (profiles == null)
					System.setProperty(ACTIVE_PROFILES_PROPERTY_NAME, "dev");
			}
		}
		SpringApplication.run(MainApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		if (isDevelopment() || isUnitTest()) {
			UserRepository userRepository = context.getBean(UserRepository.class);
			PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
			if (userRepository.count() == 0) {
				User user = new User();
				user.setUsername(USER_USERNAME);
				user.setName(user.getUsername());
				user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
				userRepository.save(user);
				User admin = new User();
				admin.setUsername(ADMIN_USERNAME);
				admin.setName(user.getUsername());
				admin.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
				admin.setRoles(Collections.singleton(ADMIN_ROLE));
				userRepository.save(admin);
			}
		}
	}

	public static final String DEFAULT_PASSWORD = "password";
	public static final String USER_USERNAME = "user";
	public static final String ADMIN_USERNAME = "admin";
	public static final String ADMIN_ROLE = "ADMIN";
}
