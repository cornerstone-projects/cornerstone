package io.cornerstone.core.hibernate.type;

import java.util.List;

import javax.persistence.Entity;

import io.cornerstone.core.hibernate.domain.AbstractEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@SuppressWarnings("deprecation")
@Entity
@Getter
@Setter
class TestEntity extends AbstractEntity {

	private static final long serialVersionUID = 6471017006033411659L;

	@Type(type = "json")
	private List<TestComponent> testComponentList;

}
