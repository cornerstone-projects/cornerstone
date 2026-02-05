package io.cornerstone.core.persistence.domain;

import java.io.Serializable;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import io.cornerstone.core.domain.Ordered;
import io.cornerstone.core.domain.Treeable;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.json.FromIdDeserializer;
import io.cornerstone.core.json.ToIdSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import org.springframework.data.domain.Persistable;

import static lombok.AccessLevel.PROTECTED;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractTreeableEntity<T extends AbstractTreeableEntity<T>> extends AbstractAuditable<Long>
		implements Treeable<T, Long>, Ordered<T>, Serializable {

	private static final long serialVersionUID = -2016525006418883120L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@JsonView(Persistable.class)
	@Setter(PROTECTED)
	private @Nullable Long id;

	@Column(unique = true, nullable = false)
	@JsonIgnore
	protected String fullId;

	@Column(nullable = false)
	@JsonView(View.Edit.class)
	protected String name;

	protected Integer level;

	@JsonView(View.Edit.class)
	protected Integer displayOrder;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
	@JsonView(View.Edit.class)
	@JsonSerialize(using = ToIdSerializer.class)
	@JsonDeserialize(using = FromIdDeserializer.class)
	@Schema(type = "integer", format = "int64", example = "1")
	protected T parent;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "parent")
	@OrderBy("displayOrder,name")
	protected Collection<T> children;

}
