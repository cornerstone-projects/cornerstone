package io.cornerstone.core.security;

import java.util.Collections;
import java.util.List;

import lombok.Setter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

import static java.util.stream.Collectors.toList;

public class DefaultDaoAuthenticationProvider extends DaoAuthenticationProvider {

	@Setter
	private List<UserAuthorityMapper> userAuthorityMappers = Collections.emptyList();

	@Setter
	private List<VerificationCodeChecker> verificationCodeCheckers = Collections.emptyList();

	@Override
	protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
			UserDetails user) {
		Authentication auth = super.createSuccessAuthentication(principal, authentication, user);
		List<GrantedAuthority> list = this.userAuthorityMappers.stream()
				.flatMap(mapper -> mapper.mapAuthorities(user).stream()).collect(toList());
		list.addAll(auth.getAuthorities());
		UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(principal,
				auth.getCredentials(), list);
		result.setDetails(auth.getDetails());
		return result;
	}

	@Override
	public UserDetailsChecker getPreAuthenticationChecks() {
		return super.getPreAuthenticationChecks();
	}

	@Override
	public UserDetailsChecker getPostAuthenticationChecks() {
		return super.getPostAuthenticationChecks();
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		if (authentication.getDetails() instanceof DefaultWebAuthenticationDetails) {
			String verificationCode = ((DefaultWebAuthenticationDetails) authentication.getDetails())
					.getVerificationCode();
			boolean skipPasswordCheck = false;
			AuthenticationException ex = null;
			for (VerificationCodeChecker checker : this.verificationCodeCheckers) {
				if (!checker.skip(userDetails)) {
					try {
						checker.verify(userDetails, authentication, verificationCode);
						if (checker.skipPasswordCheck(userDetails)) {
							skipPasswordCheck = true;
						}
						ex = null;
						break;
					}
					catch (AuthenticationException ex2) {
						ex = ex2;
						continue;
					}
				}
			}
			if (ex != null) {
				throw ex;
			}
			if (!skipPasswordCheck) {
				super.additionalAuthenticationChecks(userDetails, authentication);
			}
		}
		else {
			super.additionalAuthenticationChecks(userDetails, authentication);
		}
	}

}
