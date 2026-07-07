package io.cornerstone.core.persistence.type;

import java.util.List;

import io.cornerstone.core.persistence.domain.AbstractEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
class TestEntity extends AbstractEntity {

	private static final long serialVersionUID = 6471017006033411659L;

	@Json
	private List<TestComponent> testComponentList;

}
