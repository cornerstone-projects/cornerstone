package io.cornerstone.core.hibernate.id.sequence;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TestEntity implements Persistable<Long>, Serializable {

	private static final long serialVersionUID = 6471017006033411659L;

	@Id
	@GeneratedValue(generator = "sequence")
	@GenericGenerator(name = "sequence", strategy = "io.cornerstone.core.hibernate.id.SequenceIdentifierGenerator", parameters = @Parameter(name = "sequenceName", value = "testSequence"))
	@JsonProperty(access = Access.WRITE_ONLY)
	private @Nullable Long id;

	@Nullable
	@Override
	public Long getId() {
		return id;
	}

	protected void setId(@Nullable Long id) {
		this.id = id;
	}

	@Transient
	@Override
	@JsonIgnore
	public boolean isNew() {
		return null == getId();
	}
}