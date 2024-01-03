package io.cornerstone.core.persistence.type;

import java.util.List;

import io.cornerstone.core.persistence.domain.AbstractEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
class TestEntity extends AbstractEntity {

	private static final long serialVersionUID = 6471017006033411659L;

	@Type(JsonType.class)
	private List<TestComponent> testComponentList;

}
