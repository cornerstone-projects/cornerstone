package io.cornerstone.core.web.controller.readable;

import javax.persistence.Entity;

import io.cornerstone.core.hibernate.domain.AbstractEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
class TestEntity extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	private String name;

}
