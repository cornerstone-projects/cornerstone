package com.example.demo;

import java.util.Optional;

import javax.validation.constraints.Min;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@GetMapping("/users")
	public Iterable<User> list() {
		return userRepository.findAll();
	}

	@GetMapping("/user/{id}")
	public Optional<User> get(@PathVariable Long id) {
		return userRepository.findById(id);
	}

	@PostMapping("/users")
	public User save(@RequestBody User user) {
		return userRepository.save(user);
	}

	@PutMapping("/user/{id}")
	public User update(@Min(1) @PathVariable Long id, @RequestBody User user) {
		return userRepository.findById(id).map(u -> {
			try {
				BeanUtils.copyProperties(user, u);
			} catch (Exception e) {
				e.printStackTrace();
			}
			userRepository.save(u);
			return u;
		}).orElseThrow(() -> new IllegalArgumentException("Invalid id: " + id));
	}

}
