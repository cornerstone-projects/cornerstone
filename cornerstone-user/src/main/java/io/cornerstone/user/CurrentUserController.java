package io.cornerstone.user;

import com.fasterxml.jackson.annotation.JsonView;
import io.cornerstone.core.util.BeanUtils;
import io.cornerstone.core.web.BaseRestController;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

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

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

@RestController
@Validated
public class CurrentUserController extends BaseRestController {

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
		return this.userRepository.findByUsername(currentUser.getUsername()).map(u -> {
			BeanUtils.copyNonNullProperties(user, u);
			// synchronize user in session
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					BeanUtils.copyNonNullProperties(user, currentUser);
					RequestAttributes attrs = RequestContextHolder.currentRequestAttributes();
					Object securityContext = attrs.getAttribute(SPRING_SECURITY_CONTEXT_KEY, SCOPE_SESSION);
					if (securityContext != null) {
						// trigger session save to store
						attrs.setAttribute(SPRING_SECURITY_CONTEXT_KEY, securityContext, SCOPE_SESSION);
					}
				}
			});
			return this.userRepository.save(u);
		}).orElseThrow(this::shouldNeverHappen);
	}

	@PutMapping(PATH_PASSWORD)
	public void changePassword(@AuthenticationPrincipal User currentUser,
			@RequestBody @Valid ChangePasswordRequest request) {
		if (request.isWrongConfirmedPassword()) {
			throw badRequest("wrong.confirmed.password");
		}
		if (!this.passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
			throw badRequest("wrong.current.password");
		}
		this.userRepository.findByUsername(currentUser.getUsername()).map(user -> {
			user.setPassword(this.passwordEncoder.encode(request.getPassword()));
			return this.userRepository.save(user);
		}).orElseThrow(this::shouldNeverHappen);
	}

}
