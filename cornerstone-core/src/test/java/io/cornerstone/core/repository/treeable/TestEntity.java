package io.cornerstone.core.repository.treeable;

import io.cornerstone.core.hibernate.domain.AbstractTreeableEntity;
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

}
