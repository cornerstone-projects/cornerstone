package io.cornerstone.core.util;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSerializationUtilsTests {

	enum Status {

		ACTIVE, DISABLED;

		@Override
		public String toString() {
			return name().toLowerCase(Locale.ROOT);
		}

	}

	@Getter
	@Setter
	public static class User {

		private String username;

		private String password;

		private int age;

		private Status status;

		private String content;

		private Date date = new Date();

		@JsonIgnore
		public String getPassword() {
			return this.password;
		}

		@JsonProperty
		public void setPassword(String password) {
			this.password = password;
		}

	}

	@Data
	static class TemporalObject {

		private LocalDate date = LocalDate.now();

		private LocalDateTime datetime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

		private LocalTime time = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);

		private YearMonth month = YearMonth.now();

		private Duration duration = Duration.ofMillis(1000);

	}

	@Data
	static class ImmutableObject {

		private final long id;

		private final String name;

	}

	@Test
	void testJson() throws IOException {
		User u = new User();
		u.setUsername("username");
		u.setPassword("password");
		u.setStatus(Status.ACTIVE);
		u.setAge(12);
		u.setContent("this is a lob");
		User u2 = (User) JsonSerializationUtils.deserialize(JsonSerializationUtils.serialize(u));
		assertThat(u2.getUsername()).isEqualTo(u.getUsername());
		assertThat(u2.getAge()).isEqualTo(u.getAge());
		assertThat(u2.getStatus()).isEqualTo(u.getStatus());
		assertThat(u2.getDate()).isEqualTo(u.getDate());
		assertThat(u2.getPassword()).isEqualTo(u.getPassword());
		assertThat(u2.getContent()).isEqualTo(u.getContent());
	}

	@Test
	void testTemporal() throws IOException {
		TemporalObject object = new TemporalObject();
		TemporalObject to2 = (TemporalObject) JsonSerializationUtils
				.deserialize(JsonSerializationUtils.serialize(object));
		assertThat(to2).isEqualTo(object);
	}

	@Test
	void testImmutable() throws IOException {
		ImmutableObject object = new ImmutableObject(12, "test");
		assertThat(JsonSerializationUtils.deserialize(JsonSerializationUtils.serialize(object))).isEqualTo(object);
	}

}
