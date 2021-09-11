package io.cornerstone.core.repository.streamable;

import javax.persistence.Entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
class TestEntity extends AbstractPersistable<Long> {

	private int index;

}