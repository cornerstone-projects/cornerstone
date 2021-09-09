package io.cornerstone.core.security;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.cornerstone.core.util.ReflectionUtils;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class UsernameAndTypeRoleMapper implements UserRoleMapper {

	@Override
	public Collection<String> mapRoles(UserDetails user) {
		return Arrays.asList(mapUsername(user), mapUserType(user));
	}

	public static String mapUsername(UserDetails user) {
		return "USERNAME(" + user.getUsername() + ")";
	}

	public static String mapUserType(UserDetails user) {
		Class<?> c = ReflectionUtils.getEntityClass(user);
		String name = c.getSimpleName();
		if (!StringUtils.hasLength(name)) // anonymous class
			name = c.getSuperclass().getSimpleName();
		return name.toUpperCase();
	}

}
