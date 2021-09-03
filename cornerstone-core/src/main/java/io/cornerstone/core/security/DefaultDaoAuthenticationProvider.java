package io.cornerstone.core.security;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Setter;

public class DefaultDaoAuthenticationProvider extends DaoAuthenticationProvider {

	@Setter
	private List<UserAuthorityMapper> userAuthorityMappers = Collections.emptyList();

	@Override
	protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
			UserDetails user) {
		Authentication auth = super.createSuccessAuthentication(principal, authentication, user);
		List<GrantedAuthority> list = userAuthorityMappers.stream().flatMap(mapper -> mapper.mapAuthorities(user).stream())
				.collect(toList());
		list.addAll(auth.getAuthorities());
		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(principal,
				auth.getCredentials(), list);
		result.setDetails(auth.getDetails());
		return result;
	}

}
