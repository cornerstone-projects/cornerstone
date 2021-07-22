package com.example.demo.user;

import java.util.Collections;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@Profile("test||dev")
@RequiredArgsConstructor
public class UserSetup {
	
	public static final String DEFAULT_PASSWORD = "password";
	public static final String USER_USERNAME = "user";
	public static final String ADMIN_USERNAME = "admin";
	public static final String ADMIN_ROLE = "ADMIN";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@PostConstruct
	void setup() {
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
