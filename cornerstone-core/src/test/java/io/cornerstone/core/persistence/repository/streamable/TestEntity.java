package io.cornerstone.core.persistence.repository.streamable;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Setter
@Getter
class TestEntity extends AbstractPersistable<Long> {

	private int index;

}
