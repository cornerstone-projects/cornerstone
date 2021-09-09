package io.cornerstone.core.hibernate.event;

import javax.persistence.Entity;

import io.cornerstone.core.hibernate.domain.AbstractAuditableEntity;
import lombok.Getter;
import lombok.Setter;

@PublishAware
@Entity
@Getter
@Setter
public class TestEntity extends AbstractAuditableEntity {

	private static final long serialVersionUID = -4902047633960048660L;

	private String name;

}