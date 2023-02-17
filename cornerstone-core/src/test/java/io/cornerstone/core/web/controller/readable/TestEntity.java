package io.cornerstone.core.web.controller.readable;

import io.cornerstone.core.hibernate.domain.AbstractEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
class TestEntity extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	private String name;

}
