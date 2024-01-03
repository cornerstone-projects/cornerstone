package io.cornerstone.core.web.controller.treeable;

import io.cornerstone.core.persistence.domain.AbstractTreeableEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
class TestEntity extends AbstractTreeableEntity<TestEntity> {

	private static final long serialVersionUID = 1L;

	TestEntity(String name) {
		this.name = name;
	}

	TestEntity(String name, int displayOrder) {
		this.name = name;
		this.displayOrder = displayOrder;
	}

	TestEntity(TestEntity parent, String name, int displayOrder) {
		this.parent = parent;
		this.name = name;
		this.displayOrder = displayOrder;
	}

}
