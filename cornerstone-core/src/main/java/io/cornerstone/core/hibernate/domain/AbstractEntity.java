package io.cornerstone.core.hibernate.domain;

import static lombok.AccessLevel.PROTECTED;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonView;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractEntity extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 3494244656461491770L;

	@Id
	@GeneratedValue(generator = "snowflake")
	@GenericGenerator(name = "snowflake", strategy = "io.cornerstone.core.hibernate.id.SnowflakeIdentifierGenerator")
	@JsonProperty(access = Access.WRITE_ONLY)
	@Setter(PROTECTED)
	private @Nullable Long id;

	@Nullable
	@JsonProperty("id")
	@JsonView(Persistable.class)
	protected String getIdAsString() {
		return id != null ? String.valueOf(id) : null;
	}

}
