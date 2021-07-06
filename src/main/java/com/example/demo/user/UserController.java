package com.example.demo.user;

import static com.example.demo.MainApplication.ADMIN_ROLE;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.core.hibernate.domain.ResultPage;

@RestController
@Validated
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/users")
	public ResultPage<User> list(@Min(1) @RequestParam(required = false, defaultValue = "1") int pageNo,
			@Min(10) @Max(100) @RequestParam(required = false, defaultValue = "10") int pageSize,
			@RequestParam(required = false) String query) {
		PageRequest pageRequest = PageRequest.of(pageNo - 1, pageSize, Sort.by("username").ascending());
		String q = '%' + query + '%';
		Page<User> page;
		if (query != null && !query.trim().isEmpty()) {
			Specification<User> spec = (root, cq, cb) -> cb.or(cb.like(root.get("username"), q),
					cb.like(root.get("name"), q));
			page = userRepository.findAll(spec, pageRequest);
		} else {
			page = userRepository.findAll(pageRequest);
		}
		return ResultPage.of(page);
	}

	@GetMapping("/user/{id}")
	public User get(@Min(1) @PathVariable Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID not found: " + id));
	}

	@PostMapping("/users")
	@PreAuthorize("hasRole('" + ADMIN_ROLE + "')")
	public User save(@RequestBody User user) {
		return userRepository.save(user);
	}

	@PutMapping("/user/{id}")
	@PreAuthorize("hasRole('" + ADMIN_ROLE + "')")
	public void update(@Min(1) @PathVariable Long id, @RequestBody User user) {
		userRepository.findById(id).map(u -> {
			BeanUtils.copyProperties(user, u, "id", "username", "password");
			return userRepository.save(u);
		}).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID not found: " + id));
	}

	@DeleteMapping("/user/{id}")
	@PreAuthorize("hasRole('" + ADMIN_ROLE + "')")
	public void delete(@Min(1) @PathVariable Long id) {
		// do not use deleteById, not annotated by @Cacheable
		userRepository.delete(userRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ID not found: " + id)));
	}

}
