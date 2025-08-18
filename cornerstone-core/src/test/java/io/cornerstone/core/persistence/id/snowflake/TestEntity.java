package io.cornerstone.core.persistence.id.snowflake;

import io.cornerstone.core.persistence.domain.AbstractEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TestEntity extends AbstractEntity {

	private static final long serialVersionUID = 6471017006033411659L;

	public void setId(Long id) {
		this.id = id;
	}

}
