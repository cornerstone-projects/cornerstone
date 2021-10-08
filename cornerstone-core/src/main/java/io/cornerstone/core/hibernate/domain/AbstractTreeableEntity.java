package io.cornerstone.core.hibernate.domain;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.cornerstone.core.domain.Ordered;
import io.cornerstone.core.domain.Treeable;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.json.FromIdDeserializer;
import io.cornerstone.core.json.ToIdSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

import static lombok.AccessLevel.PROTECTED;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractTreeableEntity<T extends AbstractTreeableEntity<T>> extends AbstractAuditable<Long>
		implements Treeable<T, Long>, Ordered<T>, Serializable {

	private static final long serialVersionUID = -2016525006418883120L;

	@Id
	@GeneratedValue
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
	@ApiModelProperty(dataType = "java.lang.Long", example = "1")
	protected T parent;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "parent")
	@OrderBy("displayOrder,name")
	protected Collection<T> children;

}
