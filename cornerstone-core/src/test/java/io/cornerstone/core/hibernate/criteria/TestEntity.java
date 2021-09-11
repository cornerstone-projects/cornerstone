package io.cornerstone.core.hibernate.criteria;

import java.util.Set;

import javax.persistence.Entity;

import io.cornerstone.core.hibernate.domain.AbstractEntity;
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