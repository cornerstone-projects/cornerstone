package io.cornerstone.user;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component("userDetailsService")
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService, UserDetailsPasswordService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return this.userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
	}

	@Override
	public UserDetails updatePassword(UserDetails user, String newPassword) {
		String username = user.getUsername();
		User u = this.userRepository.findByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException(username));
		u.setPassword(newPassword);
		this.userRepository.save(u);
		return u;
	}

}
