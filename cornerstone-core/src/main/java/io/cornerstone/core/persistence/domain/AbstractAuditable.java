package io.cornerstone.core.persistence.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonView;
import io.cornerstone.core.domain.Auditable;
import io.cornerstone.core.persistence.audit.CreationUser;
import io.cornerstone.core.persistence.audit.UpdateUser;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractAuditable<ID extends Serializable> extends AbstractPersistable<ID> implements Auditable {

	private static final long serialVersionUID = -8724343075461160257L;

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

}
