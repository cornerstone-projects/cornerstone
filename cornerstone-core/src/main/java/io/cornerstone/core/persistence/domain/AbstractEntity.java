package io.cornerstone.core.persistence.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonView;
import io.cornerstone.core.persistence.id.SnowflakeIdentifier;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

import static lombok.AccessLevel.PROTECTED;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractEntity extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 3494244656461491770L;

	@Id
	@SnowflakeIdentifier
	@JsonProperty(access = Access.WRITE_ONLY)
	@Setter(PROTECTED)
	private @Nullable Long id;

	@Nullable
	@JsonProperty("id")
	@JsonView(Persistable.class)
	protected String getIdAsString() {
		return this.id != null ? String.valueOf(this.id) : null;
	}

}
