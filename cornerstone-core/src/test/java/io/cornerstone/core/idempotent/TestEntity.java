package io.cornerstone.core.idempotent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Setter
@Getter
class TestEntity extends AbstractPersistable<Long> {

	@Column(unique = true)
	private String seqNo;

}
