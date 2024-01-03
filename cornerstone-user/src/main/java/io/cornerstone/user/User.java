package io.cornerstone.user;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import io.cornerstone.core.domain.Versioned;
import io.cornerstone.core.domain.View.Creation;
import io.cornerstone.core.domain.View.Edit;
import io.cornerstone.core.domain.View.Update;
import io.cornerstone.core.persistence.domain.AbstractAuditableEntity;
import io.cornerstone.core.security.verification.VerificationAware;
import io.cornerstone.core.validation.constraints.MobilePhoneNumber;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static java.util.stream.Collectors.toList;

@Entity
@Getter
@Setter
public class User extends AbstractAuditableEntity implements UserDetails, Versioned, VerificationAware {

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
		if (this.roles == null) {
			return Collections.emptyList();
		}
		return this.roles.stream().map(SimpleGrantedAuthority::new).collect(toList());
	}

	@JsonIgnore
	@Override
	public boolean isEnabled() {
		return (this.disabled == null) || !this.disabled;
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

	@JsonIgnore
	@Override
	public String getReceiver() {
		return getPhone();
	}

	interface View {

		interface EditableProfile {

		}

		interface Profile extends EditableProfile {

		}

	}

}
