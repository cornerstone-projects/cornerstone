package com.example.demo.user;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Version;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import com.example.demo.core.hibernate.domain.AbstractAuditableEntity;
import com.example.demo.core.validation.constraints.MobilePhoneNumber;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User extends AbstractAuditableEntity implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false, unique = true, updatable = false)
	@JsonView({ View.Creation.class, View.Profile.class })
	private String username;

	@Column(nullable = false)
	@JsonView(View.EditableProfile.class)
	private String name;

	@MobilePhoneNumber
	@JsonView(View.EditableProfile.class)
	private String phone;

	@JsonView(View.AdminEditable.class)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	@JsonView(View.AdminEditable.class)
	private Boolean disabled;

	@JsonView(View.AdminEditable.class)
	private Set<String> roles;

	@JsonView(View.Update.class)
	@Version
	private Integer version;

	@JsonIgnore
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Stream<String> stream = Stream.of(getClass().getSimpleName().toUpperCase());
		if (!CollectionUtils.isEmpty(roles))
			stream = Stream.concat(stream, roles.stream());
		return stream.map(r -> new SimpleGrantedAuthority(r)).collect(toList());
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

	interface View {

		interface EditableProfile {

		}

		interface Profile extends EditableProfile {

		}

		interface AdminEditable extends EditableProfile {

		}

		interface Creation extends AdminEditable {

		}

		interface Update extends AdminEditable {

		}

		interface List extends Persistable<Long>, Pageable, Creation, Update {

		}

	}
}