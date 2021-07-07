package com.example.demo.user;

import static com.example.demo.MainApplication.ADMIN_ROLE;

import java.beans.FeatureDescriptor;
import java.util.stream.Stream;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
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
import com.example.demo.core.web.AbstractRestController;

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
			Specification<User> spec = (root, cq, cb) -> cb.or(cb.like(root.get("username"), q),
					cb.like(root.get("name"), q));
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
	public User save(@RequestBody User user) {
		encodePassword(user);
		return userRepository.save(user);
	}

	@GetMapping(PATH_DETAIL)
	public User get(@Min(1) @PathVariable Long id) {
		return userRepository.findById(id).orElseThrow(() -> notFound(id));
	}

	@PutMapping(PATH_DETAIL)
	public void update(@Min(1) @PathVariable Long id, @RequestBody User user) {
		encodePassword(user);
		userRepository.findById(id).map(u -> {
			BeanUtils.copyProperties(user, u);
			return userRepository.save(u);
		}).orElseThrow(() -> notFound(id));
	}

	@PatchMapping(PATH_DETAIL)
	public User updatePartial(@Min(1) @PathVariable Long id, @RequestBody User user) {
		user.setUsername(null); // username not updatable
		encodePassword(user);
		BeanWrapper bw = new BeanWrapperImpl(user);
		String[] ignoreProperties = Stream.of(bw.getPropertyDescriptors()).map(FeatureDescriptor::getName)
				.filter(name -> bw.getPropertyValue(name) == null).toArray(String[]::new);
		return userRepository.findById(id).map(u -> {
			BeanUtils.copyProperties(user, u, ignoreProperties);
			return userRepository.save(u);
		}).orElseThrow(() -> notFound(id));
	}

	@PutMapping(PATH_PASSWORD)
	public void changePassword(@Min(1) @PathVariable Long id, @RequestBody @Valid PasswordChangeRequest request) {
		if (request.isWrongConfirmedPassword())
			throw invalidParam(messageSource.getMessage("wrong.confirmed.password", null, null));
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
