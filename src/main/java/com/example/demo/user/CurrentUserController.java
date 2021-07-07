package com.example.demo.user;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.core.web.AbstractRestController;

@RestController
@Validated
public class CurrentUserController extends AbstractRestController {

	public static final String PATH_PROFILE = "/user/@self";

	public static final String PATH_PASSWORD = PATH_PROFILE + "/password";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping(PATH_PROFILE)
	public User get(@AuthenticationPrincipal User currentUser) {
		return currentUser;
	}

	@PatchMapping(PATH_PROFILE)
	public User update(@AuthenticationPrincipal(expression = "username") String username, @RequestBody User user) {
		return userRepository.findByUsername(username).map(currentUser -> {
			if (user.getName() != null)
				currentUser.setName(user.getName());
			if (user.getPhone() != null)
				currentUser.setPhone(user.getPhone());
			// add more editable property if necessary
			return userRepository.save(currentUser);
		}).orElseThrow(this::shouldNeverHappen);
	}

	@PutMapping(PATH_PASSWORD)
	public void changePassword(@AuthenticationPrincipal User currentUser,
			@RequestBody @Valid PasswordChangeRequest request) {
		if (request.isWrongConfirmedPassword())
			throw invalidParam(messageSource.getMessage("wrong.confirmed.password", null, null));
		if (request.getCurrentPassword() == null)
			throw missingParam("currentPassword");
		if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword()))
			throw invalidParam(messageSource.getMessage("wrong.current.password", null, null));
		userRepository.findByUsername(currentUser.getUsername()).map(user -> {
			user.setPassword(passwordEncoder.encode(request.getPassword()));
			return userRepository.save(user);
		}).orElseThrow(this::shouldNeverHappen);
	}

}
