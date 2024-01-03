package io.example.showcase.customer;

import com.fasterxml.jackson.annotation.JsonView;
import io.cornerstone.core.domain.Versioned;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.persistence.domain.AbstractAuditableEntity;
import io.cornerstone.core.validation.constraints.CitizenIdentificationNumber;
import io.cornerstone.core.validation.constraints.MobilePhoneNumber;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Customer extends AbstractAuditableEntity implements Versioned {

	private static final long serialVersionUID = 1L;

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

}
