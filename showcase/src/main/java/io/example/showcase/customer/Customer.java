package io.example.showcase.customer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Version;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonView;

import io.cornerstone.core.hibernate.domain.AbstractAuditableEntity;
import io.cornerstone.core.validation.constraints.CitizenIdentificationNumber;
import io.cornerstone.core.validation.constraints.MobilePhoneNumber;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Customer extends AbstractAuditableEntity {

	private static final long serialVersionUID = 1L;

	@CitizenIdentificationNumber
	@JsonView(View.Creation.class)
	@Column(nullable = false, unique = true, updatable = false)
	private String idNo;

	@JsonView(View.Editable.class)
	@Column(nullable = false)
	private String name;

	@MobilePhoneNumber
	@JsonView(View.Editable.class)
	private String phone;

	@JsonView(View.Editable.class)
	private String address;

	@JsonView(View.Editable.class)
	private Boolean disabled;

	@JsonView(View.Update.class)
	@Version
	private Integer version;

	interface View {

		interface Editable {

		}

		interface Creation extends Editable {

		}

		interface Update extends Editable {

		}

		interface List extends Persistable<Long>, Pageable, Creation, Update {

		}

	}

}