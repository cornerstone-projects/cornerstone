package io.cornerstone.core.persistence.event;

import io.cornerstone.core.persistence.domain.AbstractAuditableEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@PublishAware
@Entity
@Getter
@Setter
class TestEntity extends AbstractAuditableEntity {

	private static final long serialVersionUID = -4902047633960048660L;

	private String name;

}
