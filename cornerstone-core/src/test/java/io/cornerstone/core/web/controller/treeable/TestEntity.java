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
public class TestEntity extends AbstractTreeableEntity<TestEntity> {

	private static final long serialVersionUID = 1L;

	public TestEntity(String name) {
		this.name = name;
	}

	public TestEntity(String name, int displayOrder) {
		this.name = name;
		this.displayOrder = displayOrder;
	}

	public TestEntity(TestEntity parent, String name, int displayOrder) {
		this.parent = parent;
		this.name = name;
		this.displayOrder = displayOrder;
	}

}