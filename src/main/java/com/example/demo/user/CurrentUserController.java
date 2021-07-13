package com.example.demo.user;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.example.demo.core.util.BeanUtils;
import com.example.demo.core.web.AbstractRestController;
import com.fasterxml.jackson.annotation.JsonView;

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
	@JsonView({ User.View.Profile.class })
	public User get(@AuthenticationPrincipal User currentUser) {
		return currentUser;
	}

	@PatchMapping(PATH_PROFILE)
	@Transactional
	@JsonView(User.View.Profile.class)
	public User update(@AuthenticationPrincipal User currentUser,
			@RequestBody @JsonView(User.View.EditableProfile.class) @Valid User user) {
		return userRepository.findByUsername(currentUser.getUsername()).map(u -> {
			BeanUtils.copyNonNullProperties(user, u);
			// synchronize user in session
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				public void afterCommit() {
					BeanUtils.copyNonNullProperties(user, currentUser);
					RequestAttributes attrs = RequestContextHolder.currentRequestAttributes();
					// trigger session save to store
					attrs.setAttribute(SPRING_SECURITY_CONTEXT_KEY,
							attrs.getAttribute(SPRING_SECURITY_CONTEXT_KEY, SCOPE_SESSION), SCOPE_SESSION);
				}
			});
			return userRepository.save(u);
		}).orElseThrow(this::shouldNeverHappen);
	}

	@PutMapping(PATH_PASSWORD)
	public void changePassword(@AuthenticationPrincipal User currentUser,
			@RequestBody @Valid ChangePasswordRequest request) {
		if (request.isWrongConfirmedPassword())
			throw badRequest("wrong.confirmed.password");
		if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword()))
			throw badRequest("wrong.current.password");
		userRepository.findByUsername(currentUser.getUsername()).map(user -> {
			user.setPassword(passwordEncoder.encode(request.getPassword()));
			return userRepository.save(user);
		}).orElseThrow(this::shouldNeverHappen);
	}

}
