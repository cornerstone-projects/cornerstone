package io.cornerstone.core.hibernate.id.sequence;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

@Entity
@Getter
@Setter
class TestEntity implements Persistable<Long>, Serializable {

	private static final long serialVersionUID = 6471017006033411659L;

	@Id
	@GeneratedValue(generator = "sequence")
	@GenericGenerator(name = "sequence", strategy = "io.cornerstone.core.hibernate.id.SequenceIdentifierGenerator",
			parameters = @Parameter(name = "sequenceName", value = "testSequence"))
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
