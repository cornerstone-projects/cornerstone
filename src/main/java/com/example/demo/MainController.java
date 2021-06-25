package com.example.demo;

import static com.example.demo.Application.ADMIN_ROLE;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/")
	public String home() {
		return "home";
	}

	@GetMapping("/admin")
	@PreAuthorize("hasRole('" + ADMIN_ROLE + "')")
	public String admin() {
		return "admin";
	}

}
