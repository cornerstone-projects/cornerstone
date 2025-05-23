package io.cornerstone.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.web.AbstractEntityController;
import io.cornerstone.core.web.PageableAsQueryParam;
import io.cornerstone.core.web.SortAsQueryParam;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.BatchSettings;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static io.cornerstone.user.UserSetup.ADMIN_ROLE;
import static io.cornerstone.user.User_.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.HttpStatus.OK;

@RequiredArgsConstructor
@RestController
@Secured(ADMIN_ROLE)
public class UserController extends AbstractEntityController<User, Long> {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	@Override
	@GetMapping(PATH_LIST)
	@JsonView({ View.List.class })
	@PageableAsQueryParam
	public ResultPage<User> list(@PageableDefault(sort = USERNAME, direction = ASC) Pageable pageable,
			@RequestParam(required = false) String query, @Parameter(hidden = true) User example) {
		// use "username asc" override default sort "id desc"
		return super.list(pageable, query, example);
	}

	@PutMapping(PATH_DETAIL + "/password")
	public void updatePassword(@PathVariable Long id, @RequestBody @Valid UpdatePasswordRequest request) {
		if (request.isWrongConfirmedPassword()) {
			throw badRequest("wrong.confirmed.password");
		}
		this.userRepository.findById(id).map(user -> {
			user.setPassword(request.getPassword());
			encodePassword(user);
			return this.userRepository.save(user);
		}).orElseThrow(() -> notFound(id));
	}

	@GetMapping(value = PATH_LIST + ".csv", produces = "text/csv")
	@SortAsQueryParam
	public ResponseEntity<StreamingResponseBody> download(@SortDefault(sort = "id") Sort sort,
			@RequestParam(required = false) Charset charset, @Parameter(hidden = true) User example) {
		Charset cs = (charset != null ? charset : StandardCharsets.UTF_8);
		return ResponseEntity.status(OK).contentType(new MediaType("text", "csv", cs)).body(os -> {
			try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, cs), true)) {
				writer.write("id,username,name,phone,roles,disabled");
				this.userRepository.forEach(Example.of(example, getExampleMatcher()), sort, u -> {
					writer.write('\n');
					writer.write(String.format("%s,%s,%s,%s,%s,%b", u.getId(), u.getUsername(), u.getName(),
							u.getPhone(), u.getRoles() != null ? String.join(" ", u.getRoles()) : "", u.getDisabled()));
				});
			}
		});
	}

	@PostMapping(value = PATH_LIST + ".csv", consumes = "text/csv")
	public void upload(@RequestBody InputStreamResource input) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(input.getInputStream(), StandardCharsets.UTF_8))) {
			int batchSize = this.applicationContext.getEnvironment()
				.getProperty("spring.jpa.properties." + BatchSettings.STATEMENT_BATCH_SIZE, Integer.class, 50);
			String line;
			List<User> batch = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				String[] arr = line.split(",");
				User user = new User();
				user.setUsername(arr[0]);
				user.setName(arr[1]);
				user.setPhone(arr[2]);
				String roles = arr[3];
				if (roles.startsWith("\"") && roles.endsWith("\"")) {
					roles = roles.substring(1, roles.length() - 1);
				}
				if (!roles.isEmpty()) {
					user.setRoles(new LinkedHashSet<>(List.of(roles.split(" "))));
				}
				user.setDisabled(Boolean.valueOf(arr[4]));
				batch.add(user);
				if (batch.size() == batchSize) {
					this.userRepository.saveAll(batch);
					batch.clear();
				}
			}
			if (!batch.isEmpty()) {
				this.userRepository.saveAll(batch);
			}
		}
		catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private void encodePassword(User user) {
		if (StringUtils.hasLength(user.getPassword())) {
			user.setPassword(this.passwordEncoder.encode(user.getPassword()));
		}
	}

	@Override
	protected void beforeSave(User user) {
		if (this.userRepository.existsByUsername(user.getUsername())) {
			throw badRequest("username.already.exists");
		}
		encodePassword(user);
	}

	@Override
	protected void beforeUpdate(Long id, User user) {
		encodePassword(user);
	}

	@Override
	protected void beforeDelete(User user) {
		if (user.getDisabled() != Boolean.TRUE) {
			throw badRequest("disable.before.delete");
		}
	}

	@Override
	protected Specification<User> getQuerySpecification(String query) {
		String q = '%' + query + '%';
		return (root, cq, cb) -> cb.or(cb.or(cb.like(root.get(username), q), cb.like(root.get(name), q)),
				cb.equal(root.get(phone), query));
	}

	@Override
	protected ExampleMatcher getExampleMatcher() {
		return ExampleMatcher.matching()
			.withIgnorePaths(PASSWORD, ROLES)
			.withMatcher(USERNAME, match -> match.contains().ignoreCase())
			.withMatcher(NAME, ExampleMatcher.GenericPropertyMatcher::contains);
	}

}
