package io.cornerstone.user;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Version;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.cornerstone.core.domain.Versioned;
import io.cornerstone.core.domain.View.Creation;
import io.cornerstone.core.domain.View.Edit;
import io.cornerstone.core.domain.View.Update;
import io.cornerstone.core.hibernate.domain.AbstractAuditableEntity;
import io.cornerstone.core.validation.constraints.MobilePhoneNumber;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User extends AbstractAuditableEntity implements UserDetails, Versioned {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false, unique = true, updatable = false)
	@JsonView({ Creation.class, View.Profile.class })
	private String username;

	@Column(nullable = false)
	@JsonView({ Edit.class, View.EditableProfile.class })
	private String name;

	@MobilePhoneNumber
	@JsonView({ Edit.class, View.EditableProfile.class })
	private String phone;

	@JsonView(Edit.class)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String password;

	@JsonView(Edit.class)
	private Boolean disabled;

	@JsonView({ Edit.class, View.Profile.class })
	private Set<String> roles;

	@JsonView(Update.class)
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

	}
}