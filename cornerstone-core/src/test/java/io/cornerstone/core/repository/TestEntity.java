package io.cornerstone.core.repository;

import javax.persistence.Entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class TestEntity extends AbstractPersistable<Long> {

	private int index;

}