package io.cornerstone.core.security;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class DefaultAuthenticationManager extends ProviderManager {

	private static final String CACHE_NAMESPACE = "fla"; // Failed Login Attempts

	@Autowired(required = false)
	private StringRedisTemplate stringRedisTemplate;

	private final SecurityProperties properties;

	public DefaultAuthenticationManager(List<AuthenticationProvider> providers, SecurityProperties properties) {
		super(providers);
		this.properties = properties;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		if (username != null && username.length() > this.properties.getAuthentication().getUsernameMaxLength()) {
			throw new UsernameNotFoundException(this.messages.getMessage("JdbcDaoImpl.notFound",
					new Object[] { username }, "Username {0} not found"));
		}
		if (this.stringRedisTemplate == null) {
			return super.authenticate(authentication);
		}
		String key = CACHE_NAMESPACE + ":" + username;
		ValueOperations<String, String> opsForValue = this.stringRedisTemplate.opsForValue();
		Long times = opsForValue.increment(key, 0);
		int lockdownForMinutes = this.properties.getAuthentication().getLockdownForMinutes();
		this.stringRedisTemplate.expire(key, Duration.ofMinutes(lockdownForMinutes));
		int maxAttempts = this.properties.getAuthentication().getMaxAttempts();
		if (times != null && times >= maxAttempts) {
			throw new LockedException(this.messages.getMessage("DefaultAuthenticationManager.maxAttemptsExceeded",
					new Object[] { maxAttempts }, "Login attempts exceed {0}"));
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
			this.stringRedisTemplate.expire(key, Duration.ofMinutes(lockdownForMinutes));
			throw ex;
		}
	}

}
