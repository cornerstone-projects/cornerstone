package io.cornerstone.core.redis.serializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.cornerstone.core.domain.Scope;
import lombok.Data;
import org.junit.jupiter.api.Test;

import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class RedisSerializerTestBase {

	protected abstract RedisSerializer<Object> getRedisSerializer();

	@Test
	void testNullValue() {
		test(NullValue.INSTANCE);
	}

	@Test
	void testPrimitive() {
		test(true);
		test(1);
		test(120L);
		test(1.0);
		test(20.0f);
	}

	@Test
	void testSimpleObject() {
		test(Scope.GLOBAL);
		test(new Date());
		test(new BigDecimal("100.00"));
		test(new AtomicInteger(100));
	}

	@Test
	void testDateAndTime() {
		test(LocalDate.now());
		test(LocalDateTime.now());
	}

	@Test
	void testCollectionObject() {
		ArrayList<GrantedAuthority> list = new ArrayList<>();
		list.add(new SimpleGrantedAuthority("test"));
		test(list);
		HashMap<String, GrantedAuthority> map = new HashMap<>();
		map.put("test", new SimpleGrantedAuthority("test"));
		test(map);
	}

	@Test
	void testComplexObject() {
		User u = new User();
		u.setUsername("test");
		u.setPassword("test");
		u.setAge(100);
		u.setStatus(Status.DISABLED);
		u.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("test")));
		u.setCreated(LocalDateTime.now());
		test(u);
	}

	private void test(Object obj) {
		RedisSerializer<Object> serializer = getRedisSerializer();
		Object obj2 = serializer.deserialize(serializer.serialize(obj));
		if (obj instanceof NullValue || obj instanceof Enum) {
			assertThat(obj2).isSameAs(obj);
		}
		if (obj instanceof AtomicInteger) { // AtomicInteger not override equals
			assertThat(((AtomicInteger) obj2).intValue()).isEqualTo(((AtomicInteger) obj).intValue());
		}
		else {
			assertThat(obj2).isEqualTo(obj);
		}
	}

	enum Status {

		ACTIVE, DISABLED

	}

	@Data
	public static class User implements UserDetails {

		private static final long serialVersionUID = 1L;

		private String username;

		private String password;

		private int age;

		private Status status;

		private List<GrantedAuthority> authorities;

		private LocalDateTime created;

		@Override
		@JsonIgnore
		public String getPassword() {
			return this.password;
		}

		@Override
		public boolean isAccountNonExpired() {
			return false;
		}

		@Override
		public boolean isAccountNonLocked() {
			return false;
		}

		@Override
		public boolean isCredentialsNonExpired() {
			return false;
		}

		@Override
		public boolean isEnabled() {
			return this.status == Status.ACTIVE;
		}

		public User getSelf() {
			return this;
		}

	}

}
