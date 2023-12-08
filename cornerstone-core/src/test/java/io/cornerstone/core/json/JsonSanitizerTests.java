package io.cornerstone.core.json;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSanitizerTests {

	@Test
	void testSanitize() {
		JsonSanitizer sanitizer = new JsonSanitizer();
		String json = """
				{"password":"password"}
				""";
		assertThat(sanitizer.sanitize(json)).contains("\"******\"");
		json = """
				{"username":"username","password":"password"}
				""";
		assertThat(sanitizer.sanitize(json)).contains("\"******\"");
		json = """
				{"user":{"username":"username","password":"password"}}
				""";
		assertThat(sanitizer.sanitize(json)).contains("\"******\"");
		json = """
				{"user":{"user2":{"username":"username","password":"password"}}}
				""";
		assertThat(sanitizer.sanitize(json)).contains("\"******\"");

		sanitizer.getDropping().add((name, parent) -> name.equals("username"));
		assertThat(sanitizer.sanitize(json)).doesNotContain("\"username\"");
		sanitizer.getDropping().add((name, parent) -> name.equals("user2"));
		assertThat(sanitizer.sanitize(json)).doesNotContain("\"user2\"");
		sanitizer.getDropping().add((name, parent) -> name.equals("user"));
		assertThat(sanitizer.sanitize(json)).doesNotContain("\"user\"");
	}

	@Test
	void testToJson() {
		JsonSanitizer sanitizer = new JsonSanitizer();
		User user = new User("username", "password", 12);
		user.mate = new User("mate", "password", 11);
		assertThat(sanitizer.toJson(user)).contains("\"******\"");
		assertThat(sanitizer.toJson(user)).contains("\"mate\"");
		assertThat(sanitizer.toJson(Collections.singletonMap("user", user))).contains("\"user\"");

		sanitizer.getDropping().add((name, parent) -> name.equals("password"));
		assertThat(sanitizer.toJson(user)).doesNotContain("\"password\"");
		sanitizer.getDropping().add((name, parent) -> name.equals("mate"));
		assertThat(sanitizer.toJson(user)).doesNotContain("\"mate\"");
		sanitizer.getDropping().add((name, parent) -> name.equals("user"));
		assertThat(sanitizer.toJson(Collections.singletonMap("user", user))).doesNotContain("\"user\"");
	}

	@Test
	void testCustomize() {
		JsonSanitizer sanitizer = new JsonSanitizer();
		Map<BiPredicate<String, Object>, Function<String, String>> mapping = sanitizer.getMapping();
		mapping.clear();
		assertThat(sanitizer.toJson(new User("username", "password", 12))).doesNotContain("\"******\"");
		mapping.put((s, obj) -> s.equals("username") && (obj instanceof User), s -> "------");
		mapping.put((s, obj) -> s.equals("age") && (obj instanceof User), s -> "0.0");
		String json = sanitizer.toJson(new User("myname", "mypass", 12));
		assertThat(json).contains("------");
		assertThat(json).contains("\"mypass\"");
		assertThat(json).doesNotContain("12");
		assertThat(json).contains("0.0");
		sanitizer.getDropping().add((s, obj) -> s.equals("age") && (obj instanceof User));
		json = sanitizer.toJson(new User("myname", "mypass", 12));
		assertThat(json).contains("------");
		assertThat(json).contains("\"mypass\"");
		assertThat(json).doesNotContain("age");
	}

	@Test
	void testToJsonWithAnnotation() {
		JsonSanitizer sanitizer = new JsonSanitizer();
		Person p = new Person("test", "13333333333", 12);
		String json = sanitizer.toJson(p);
		assertThat(json).contains("\"1**********\"");
		assertThat(json).doesNotContain("age");
	}

	@RequiredArgsConstructor
	@Getter
	static class User {

		private final String username;

		private final String password;

		private final int age;

		private User mate;

	}

	@RequiredArgsConstructor
	@Getter
	static class Person {

		private final String name;

		@JsonSanitize("1**********")
		private final String phone;

		@JsonSanitize
		private final int age;

	}

}
