package io.cornerstone.core.json;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonDesensitizerTests {

	@Test
	void testDesensitize() {
		JsonDesensitizer desensitizer = new JsonDesensitizer();
		String json = "{\"password\":\"password\"}";
		assertThat(desensitizer.desensitize(json)).contains("\"******\"");
		json = "{\"username\":\"username\",\"password\":\"password\"}";
		assertThat(desensitizer.desensitize(json)).contains("\"******\"");
		json = "{\"user\":{\"username\":\"username\",\"password\":\"password\"}}";
		assertThat(desensitizer.desensitize(json)).contains("\"******\"");
		json = "{\"user\":{\"user2\":{\"username\":\"username\",\"password\":\"password\"}}}";
		assertThat(desensitizer.desensitize(json)).contains("\"******\"");

		desensitizer.getDropping().add((name, parent) -> name.equals("username"));
		assertThat(desensitizer.desensitize(json)).doesNotContain("\"username\"");
		desensitizer.getDropping().add((name, parent) -> name.equals("user2"));
		assertThat(desensitizer.desensitize(json)).doesNotContain("\"user2\"");
		desensitizer.getDropping().add((name, parent) -> name.equals("user"));
		assertThat(desensitizer.desensitize(json)).doesNotContain("\"user\"");
	}

	@Test
	void testToJson() {
		JsonDesensitizer desensitizer = new JsonDesensitizer();
		User user = new User("username", "password", 12);
		User mate = new User("mate", "password", 11);
		user.mate = mate;
		assertThat(desensitizer.toJson(user)).contains("\"******\"");
		assertThat(desensitizer.toJson(user)).contains("\"mate\"");
		assertThat(desensitizer.toJson(Collections.singletonMap("user", user))).contains("\"user\"");

		desensitizer.getDropping().add((name, parent) -> name.equals("password"));
		assertThat(desensitizer.toJson(user)).doesNotContain("\"password\"");
		desensitizer.getDropping().add((name, parent) -> name.equals("mate"));
		assertThat(desensitizer.toJson(user)).doesNotContain("\"mate\"");
		desensitizer.getDropping().add((name, parent) -> name.equals("user"));
		assertThat(desensitizer.toJson(Collections.singletonMap("user", user))).doesNotContain("\"user\"");
	}

	@Test
	void testCustomize() {
		JsonDesensitizer desensitizer = new JsonDesensitizer();
		Map<BiPredicate<String, Object>, Function<String, String>> mapping = desensitizer.getMapping();
		mapping.clear();
		assertThat(desensitizer.toJson(new User("username", "password", 12))).doesNotContain("\"******\"");
		mapping.put((s, obj) -> s.equals("username") && (obj instanceof User), s -> "------");
		mapping.put((s, obj) -> s.equals("age") && (obj instanceof User), s -> "0.0");
		String json = desensitizer.toJson(new User("myname", "mypass", 12));
		assertThat(json).contains("------");
		assertThat(json).contains("\"mypass\"");
		assertThat(json).doesNotContain("12");
		assertThat(json).contains("0.0");
		desensitizer.getDropping().add((s, obj) -> s.equals("age") && (obj instanceof User));
		json = desensitizer.toJson(new User("myname", "mypass", 12));
		assertThat(json).contains("------");
		assertThat(json).contains("\"mypass\"");
		assertThat(json).doesNotContain("age");
	}

	@Test
	void testToJsonWithAnnotation() {
		JsonDesensitizer desensitizer = new JsonDesensitizer();
		Person p = new Person("test", "13333333333", 12);
		String json = desensitizer.toJson(p);
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

		@JsonDesensitize("1**********")
		private final String phone;

		@JsonDesensitize
		private final int age;

	}

}
