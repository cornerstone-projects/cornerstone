package io.cornerstone.core.security;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static java.util.stream.Collectors.toList;

public class DefaultDaoAuthenticationProvider extends DaoAuthenticationProvider {

	@Value("${defaultDaoAuthenticationProvider.lockdownForMinutes:60}") // 1 hour
	private int lockdownForMinutes = 60;

	@Value("${defaultDaoAuthenticationProvider.maxAttempts:5}")
	private int maxAttempts = 5;

	@Value("${defaultDaoAuthenticationProvider.usernameMaxLength:32}")
	private int usernameMaxLength = 32;

	@Autowired(required = false)
	private StringRedisTemplate stringRedisTemplate;

	@Autowired(required = false)
	private List<UserAuthorityMapper> userAuthorityMappers = Collections.emptyList();

	@Autowired(required = false)
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
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		if (authentication.getDetails() instanceof DefaultWebAuthenticationDetails dwad) {
			String verificationCode = dwad.getVerificationCode();
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

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		if (username != null && username.length() > this.usernameMaxLength) {
			throw new UsernameNotFoundException(this.messages.getMessage("JdbcDaoImpl.notFound",
					new Object[] { username }, "Username {0} not found"));
		}
		if (this.stringRedisTemplate == null) {
			return super.authenticate(authentication);
		}
		String key = "fla:" + username;
		ValueOperations<String, String> opsForValue = this.stringRedisTemplate.opsForValue();
		Long times = opsForValue.increment(key, 0);
		this.stringRedisTemplate.expire(key, Duration.ofMinutes(this.lockdownForMinutes));
		if (times != null && times >= this.maxAttempts) {
			throw new LockedException(this.messages.getMessage("DefaultDaoAuthenticationProvider.maxAttemptsExceeded",
					new Object[] { this.maxAttempts }, "Login attempts exceed {0}"));
		}
		try {
			Authentication auth = super.authenticate(authentication);
			this.stringRedisTemplate.delete(key);
			return auth;
		}
		catch (CredentialsExpiredException ex) {
			this.stringRedisTemplate.delete(key);
			throw ex;
		}
		catch (BadCredentialsException ex) {
			opsForValue.increment(key);
			this.stringRedisTemplate.expire(key, Duration.ofMinutes(this.lockdownForMinutes));
			throw ex;
		}
	}

}
