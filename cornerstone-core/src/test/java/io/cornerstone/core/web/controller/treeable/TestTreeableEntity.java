package io.cornerstone.core.web.controller.treeable;

import javax.persistence.Entity;

import io.cornerstone.core.hibernate.domain.AbstractTreeableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TestTreeableEntity extends AbstractTreeableEntity<TestTreeableEntity> {

	private static final long serialVersionUID = 1L;

	public TestTreeableEntity(String name) {
		this.name = name;
	}

	public TestTreeableEntity(String name, int displayOrder) {
		this.name = name;
		this.displayOrder = displayOrder;
	}

	public TestTreeableEntity(TestTreeableEntity parent, String name, int displayOrder) {
		this.parent = parent;
		this.name = name;
		this.displayOrder = displayOrder;
	}

}