package io.cornerstone.core.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@FunctionalInterface
public interface UserAuthorityMapper {

	Collection<? extends GrantedAuthority> mapAuthorities(UserDetails user);

}
