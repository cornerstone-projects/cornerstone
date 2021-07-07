package com.example.demo.user;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import com.example.demo.core.hibernate.domain.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User extends AbstractEntity implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false, unique = true, updatable = false)
	private String username;

	@Column(nullable = false)
	private String name;
	
	private String phone;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	private Boolean disabled;

	private Set<String> roles;

	@JsonIgnore
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Stream<String> stream = Stream.of(getClass().getSimpleName().toUpperCase());
		if (!CollectionUtils.isEmpty(roles))
			stream = Stream.concat(stream, roles.stream());
		return stream.map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toList());
	}

	@JsonIgnore
	@Override
	public boolean isEnabled() {
		return disabled == null || !disabled;
	}

	@JsonIgnore
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@JsonIgnore
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@JsonIgnore
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

}