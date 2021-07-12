package com.example.demo.user;

import static com.example.demo.MainApplication.ADMIN_ROLE;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.core.hibernate.domain.ResultPage;
import com.example.demo.core.util.BeanUtils;
import com.example.demo.core.web.AbstractRestController;
import com.fasterxml.jackson.annotation.JsonView;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@Validated
@PreAuthorize("hasRole('" + ADMIN_ROLE + "')")
public class UserController extends AbstractRestController {

	public static final String PATH_LIST = "/users";

	public static final String PATH_DETAIL = "/user/{id:\\d+}";

	public static final String PATH_PASSWORD = PATH_DETAIL + "/password";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping(PATH_LIST)
	public ResultPage<User> list(@Min(1) @RequestParam(required = false, defaultValue = "1") int pageNo,
			@Min(10) @Max(100) @RequestParam(required = false, defaultValue = "10") int pageSize,
			@RequestParam(required = false) String query, @ApiIgnore User user) {
		PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, Sort.by("username").ascending());
		Page<User> page;
		if (StringUtils.hasText(query)) {
			String q = '%' + query + '%';
			Specification<User> spec = (root, cq, cb) -> cb.or(
					cb.or(cb.like(root.get("username"), q), cb.like(root.get("name"), q)),
					cb.equal(root.get("phone"), query));
			page = userRepository.findAll(spec, pageRequest);
		} else {
			ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("password", "roles")
					.withMatcher("username", match -> match.contains().ignoreCase())
					.withMatcher("name", match -> match.contains());
			Example<User> example = Example.of(user, matcher);
			page = userRepository.findAll(example, pageRequest);
		}
		return ResultPage.of(page);
	}

	@PostMapping(PATH_LIST)
	public User save(@RequestBody @JsonView(User.View.Createable.class) @Valid User user) {
		if (userRepository.findByUsername(user.getUsername()).isPresent())
			throw badRequest("username.already.exists");
		encodePassword(user);
		return userRepository.save(user);
	}

	@GetMapping(PATH_DETAIL)
	public User get(@Min(1) @PathVariable Long id) {
		return userRepository.findById(id).orElseThrow(() -> notFound(id));
	}

	@PutMapping(PATH_DETAIL)
	public void update(@Min(1) @PathVariable Long id,
			@RequestBody @JsonView(User.View.Updatable.class) @Valid User user) {
		encodePassword(user);
		userRepository.findById(id).map(u -> {
			BeanUtils.copyNonNullProperties(user, u);
			return userRepository.save(u);
		}).orElseThrow(() -> notFound(id));
	}

	@PatchMapping(PATH_DETAIL)
	public User updatePartial(@Min(1) @PathVariable Long id,
			@RequestBody @JsonView(User.View.Updatable.class) @Valid User user) {
		encodePassword(user);
		return userRepository.findById(id).map(u -> {
			BeanUtils.copyNonNullProperties(user, u);
			return userRepository.save(u);
		}).orElseThrow(() -> notFound(id));
	}

	@PutMapping(PATH_PASSWORD)
	public void changePassword(@Min(1) @PathVariable Long id,
			@RequestBody @JsonView(UserController.class) @Valid PasswordChangeRequest request) {
		if (request.isWrongConfirmedPassword())
			throw badRequest("wrong.confirmed.password");
		userRepository.findById(id).map(user -> {
			user.setPassword(request.getPassword());
			encodePassword(user);
			return userRepository.save(user);
		}).orElseThrow(() -> notFound(id));
	}

	@DeleteMapping(PATH_DETAIL)
	public void delete(@Min(1) @PathVariable Long id) {
		// do NOT use deleteById, not annotated by @Cacheable
		userRepository.delete(userRepository.findById(id).orElseThrow(() -> notFound(id)));
	}

	private void encodePassword(User user) {
		if (StringUtils.hasLength(user.getPassword()))
			user.setPassword(passwordEncoder.encode(user.getPassword()));
	}

}
