package io.cornerstone.core.persistence.id.sequence;

import java.io.Serializable;

import io.cornerstone.core.persistence.id.SequenceIdentifier;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

@Entity
@Getter
@Setter
class TestEntity implements Persistable<Long>, Serializable {

	private static final long serialVersionUID = 6471017006033411659L;

	@Id
	@SequenceIdentifier("testSequence")
	private @Nullable Long id;

	@Nullable
	@Override
	public Long getId() {
		return this.id;
	}

	protected void setId(@Nullable Long id) {
		this.id = id;
	}

	@Override
	public boolean isNew() {
		return null == getId();
	}

}
