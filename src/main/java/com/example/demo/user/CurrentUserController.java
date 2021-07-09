package com.example.demo.user;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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

import springfox.documentation.annotations.ApiIgnore;

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
	public User get(@AuthenticationPrincipal @ApiIgnore User currentUser) {
		return currentUser;
	}

	@PatchMapping(PATH_PROFILE)
	@Transactional
	@JsonView(User.View.Profile.class)
	public User update(@AuthenticationPrincipal @ApiIgnore User currentUser,
			@RequestBody @JsonView(User.View.EditableProfile.class) @Valid User user) {
		return userRepository.findByUsername(currentUser.getUsername()).map(u -> {
			BeanUtils.copyNonNullProperties(user, u);
			// synchronize user in session
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				public void afterCommit() {
					BeanUtils.copyNonNullProperties(user, currentUser);
					RequestAttributes attrs = RequestContextHolder.currentRequestAttributes();
					String key = HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
					// trigger session save to store
					attrs.setAttribute(key, attrs.getAttribute(key, RequestAttributes.SCOPE_SESSION),
							RequestAttributes.SCOPE_SESSION);
				}
			});
			return userRepository.save(u);
		}).orElseThrow(this::shouldNeverHappen);
	}

	@PutMapping(PATH_PASSWORD)
	public void changePassword(@AuthenticationPrincipal @ApiIgnore User currentUser,
			@RequestBody @Valid PasswordChangeRequest request) {
		if (request.isWrongConfirmedPassword())
			throw badRequest("wrong.confirmed.password");
		if (request.getCurrentPassword() == null)
			throw missingParam("currentPassword");
		if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword()))
			throw badRequest("wrong.current.password");
		userRepository.findByUsername(currentUser.getUsername()).map(user -> {
			user.setPassword(passwordEncoder.encode(request.getPassword()));
			return userRepository.save(user);
		}).orElseThrow(this::shouldNeverHappen);
	}

}
