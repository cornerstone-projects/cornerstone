package io.cornerstone.user;

import java.util.Collections;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
		if (this.userRepository.count() == 0) {
			User user = new User();
			user.setUsername(USER_USERNAME);
			user.setName(user.getUsername());
			user.setPassword(this.passwordEncoder.encode(DEFAULT_PASSWORD));
			this.userRepository.save(user);
			User admin = new User();
			admin.setUsername(ADMIN_USERNAME);
			admin.setName(user.getUsername());
			admin.setPassword(this.passwordEncoder.encode(DEFAULT_PASSWORD));
			admin.setRoles(Collections.singleton(ADMIN_ROLE));
			this.userRepository.save(admin);
		}
	}

}
