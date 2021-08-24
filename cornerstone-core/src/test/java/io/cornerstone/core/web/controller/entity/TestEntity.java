package io.cornerstone.core.web.controller.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;
import org.springframework.data.util.ProxyUtils;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonView;

import io.cornerstone.core.domain.Versioned;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.hibernate.audit.Auditable;
import io.cornerstone.core.hibernate.audit.CreationUser;
import io.cornerstone.core.hibernate.audit.UpdateUser;
import io.cornerstone.core.validation.constraints.CitizenIdentificationNumber;
import io.cornerstone.core.validation.constraints.MobilePhoneNumber;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TestEntity implements Persistable<Long>, Versioned {

	@Id
	@GeneratedValue
	@JsonView(Persistable.class)
	@Setter(AccessLevel.PROTECTED)
	private @Nullable Long id;

	@CitizenIdentificationNumber
	@JsonView(View.Creation.class)
	@Column(nullable = false, unique = true, updatable = false)
	private String idNo;

	@JsonView(View.Edit.class)
	@Column(nullable = false)
	private String name;

	@MobilePhoneNumber
	@JsonView(View.Edit.class)
	private String phone;

	@JsonView(View.Edit.class)
	private String address;

	@JsonView(View.Edit.class)
	private Boolean disabled;

	@JsonView(View.Update.class)
	@Version
	private Integer version;

	@CreationTimestamp
	@JsonView(Auditable.class)
	@Column(updatable = false)
	private LocalDateTime createdDate;

	@UpdateTimestamp
	@JsonView(Auditable.class)
	@Column(insertable = false)
	private LocalDateTime lastModifiedDate;

	@CreationUser
	@JsonView(Auditable.class)
	@Column(updatable = false)
	private String createdBy;

	@UpdateUser
	@JsonView(Auditable.class)
	@Column(insertable = false)
	private String lastModifiedBy;

	@Override
	public boolean isNew() {
		return null == getId();
	}

	@Override
	public String toString() {
		return String.format("Entity of type %s with id: %s", this.getClass().getName(), getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (!getClass().equals(ProxyUtils.getUserClass(obj))) {
			return false;
		}
		TestEntity that = (TestEntity) obj;
		return this.id == null ? false : id.equals(that.id);
	}

	@Override
	public int hashCode() {
		int hashCode = 17;
		hashCode += id == null ? 0 : id.hashCode() * 31;
		return hashCode;
	}
}