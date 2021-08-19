package io.cornerstone.user;

import static io.cornerstone.user.UserSetup.ADMIN_ROLE;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.HttpStatus.OK;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import javax.validation.Valid;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.Dialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

import io.cornerstone.core.domain.ResultPage;
import io.cornerstone.core.util.BeanUtils;
import io.cornerstone.core.web.BaseRestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@Validated
@Secured(ADMIN_ROLE)
public class UserController extends BaseRestController {

	public static final String PATH_LIST = "/users";

	public static final String PATH_DETAIL = "/user/{id:\\d+}";

	public static final String PATH_PASSWORD = PATH_DETAIL + "/password";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@GetMapping(PATH_LIST)
	@JsonView({ User.View.List.class })
	public ResultPage<User> list(@PageableDefault(sort = "username", direction = ASC) Pageable pageable,
			@RequestParam(required = false) String query, @ApiIgnore User example) {
		Page<User> page;
		if (StringUtils.hasText(query)) {
			String q = '%' + query + '%';
			Specification<User> spec = (root, cq, cb) -> cb.or(
					cb.or(cb.like(root.get("username"), q), cb.like(root.get("name"), q)),
					cb.equal(root.get("phone"), query));
			page = userRepository.findAll(spec, pageable);
		} else {
			ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("password", "roles")
					.withMatcher("username", match -> match.contains().ignoreCase())
					.withMatcher("name", match -> match.contains());
			page = userRepository.findAll(Example.of(example, matcher), pageable);
		}
		return ResultPage.of(page);
	}

	@PostMapping(PATH_LIST)
	public User save(@RequestBody @JsonView(User.View.Creation.class) @Valid User user) {
		if (userRepository.existsByUsername(user.getUsername()))
			throw badRequest("username.already.exists");
		encodePassword(user);
		return userRepository.save(user);
	}

	@GetMapping(PATH_DETAIL)
	public User get(@PathVariable Long id) {
		return userRepository.findById(id).orElseThrow(() -> notFound(id));
	}

	@PutMapping(PATH_DETAIL)
	public void update(@PathVariable Long id, @RequestBody @JsonView(User.View.Update.class) @Valid User user) {
		encodePassword(user);
		userRepository.findById(id).map(u -> {
			BeanUtils.copyPropertiesInJsonView(user, u,
					user.getVersion() == null ? User.View.Edit.class : User.View.Update.class);
			return userRepository.save(u);
		}).orElseThrow(() -> notFound(id));
	}

	@PatchMapping(PATH_DETAIL)
	public User updatePartial(@PathVariable Long id, @RequestBody @JsonView(User.View.Update.class) @Valid User user) {
		encodePassword(user);
		return userRepository.findById(id).map(u -> {
			BeanUtils.copyNonNullProperties(user, u);
			return userRepository.save(u);
		}).orElseThrow(() -> notFound(id));
	}

	@PutMapping(PATH_PASSWORD)
	public void updatePassword(@PathVariable Long id, @RequestBody @Valid UpdatePasswordRequest request) {
		if (request.isWrongConfirmedPassword())
			throw badRequest("wrong.confirmed.password");
		userRepository.findById(id).map(user -> {
			user.setPassword(request.getPassword());
			encodePassword(user);
			return userRepository.save(user);
		}).orElseThrow(() -> notFound(id));
	}

	@DeleteMapping(PATH_DETAIL)
	public void delete(@PathVariable Long id) {
		User user = userRepository.findById(id).orElseThrow(() -> notFound(id));
		if (user.isEnabled())
			throw badRequest("disable.before.delete");
		userRepository.delete(user);
	}

	@GetMapping(value = PATH_LIST + ".csv", produces = "text/csv")
	public ResponseEntity<StreamingResponseBody> download(@SortDefault(sort = "id") Sort sort,
			@RequestParam(required = false) Charset charset) {
		Charset cs = (charset != null ? charset : StandardCharsets.UTF_8);
		return ResponseEntity.status(OK).contentType(new MediaType("text", "csv", cs)).body(os -> {
			try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, cs), true)) {
				writer.write("id,username,name,phone,roles,disabled");
				userRepository.forEach(sort, u -> {
					writer.write('\n');
					writer.write(String.format("%s,%s,%s,%s,%s,%b", String.valueOf(u.getId()), u.getUsername(),
							u.getName(), u.getPhone(), u.getRoles() != null ? String.join(" ", u.getRoles()) : "",
							u.getDisabled()));
				});
			}
		});
	}

	@PostMapping(value = PATH_LIST + ".csv", consumes = "text/csv")
	public void upload(@RequestBody InputStreamResource input) {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(input.getInputStream(), StandardCharsets.UTF_8))) {
			int batchSize = applicationContext.getEnvironment().getProperty(
					"spring.jpa.properties." + AvailableSettings.STATEMENT_BATCH_SIZE, Integer.class,
					Integer.valueOf(Dialect.DEFAULT_BATCH_SIZE));
			String line;
			List<User> batch = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				String[] arr = line.split(",");
				User user = new User();
				user.setUsername(arr[0]);
				user.setName(arr[1]);
				user.setPhone(arr[2]);
				String roles = arr[3];
				if (roles.startsWith("\"") && roles.endsWith("\""))
					roles = roles.substring(1, roles.length() - 1);
				if (!roles.isEmpty())
					user.setRoles(new LinkedHashSet<>(Arrays.asList(roles.split(" "))));
				user.setDisabled(Boolean.valueOf(arr[4]));
				batch.add(user);
				if (batch.size() == batchSize) {
					userRepository.saveAll(batch);
					batch.clear();
				}
			}
			if (!batch.isEmpty()) {
				userRepository.saveAll(batch);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	private void encodePassword(User user) {
		if (StringUtils.hasLength(user.getPassword()))
			user.setPassword(passwordEncoder.encode(user.getPassword()));
	}

}
