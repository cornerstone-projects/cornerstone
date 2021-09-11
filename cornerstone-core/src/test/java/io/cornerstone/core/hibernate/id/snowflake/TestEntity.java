package io.cornerstone.core.hibernate.id.snowflake;

import javax.persistence.Entity;

import io.cornerstone.core.hibernate.domain.AbstractEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
class TestEntity extends AbstractEntity {

	private static final long serialVersionUID = 6471017006033411659L;

}