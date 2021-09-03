package io.cornerstone.core.security;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@FunctionalInterface
public interface UserRoleMapper extends UserAuthorityMapper {

	Collection<String> mapRoles(UserDetails user);

	@Override
	default Collection<? extends GrantedAuthority> mapAuthorities(UserDetails user) {
		return mapRoles(user).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
	}

}
