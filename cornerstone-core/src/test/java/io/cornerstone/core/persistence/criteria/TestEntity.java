package io.cornerstone.core.persistence.criteria;

import java.util.Set;

import io.cornerstone.core.persistence.domain.AbstractEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
class TestEntity extends AbstractEntity {

	private static final long serialVersionUID = 950904507124426155L;

	private String name;

	private Set<String> names;

	private Boolean enabled;

}
