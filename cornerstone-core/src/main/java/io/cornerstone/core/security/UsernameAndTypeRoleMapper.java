package io.cornerstone.core.security;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import io.cornerstone.core.util.ReflectionUtils;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class UsernameAndTypeRoleMapper implements UserRoleMapper {

	@Override
	public Collection<String> mapRoles(UserDetails user) {
		return List.of(mapUsername(user), mapUserType(user));
	}

	public static String mapUsername(UserDetails user) {
		return "USERNAME(" + user.getUsername() + ")";
	}

	public static String mapUserType(UserDetails user) {
		Class<?> c = ReflectionUtils.getEntityClass(user);
		String name = c.getSimpleName();
		if (!StringUtils.hasLength(name)) {
			name = c.getSuperclass().getSimpleName();
		}
		return name.toUpperCase(Locale.ROOT);
	}

}
