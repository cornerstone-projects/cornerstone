package io.cornerstone.core.hibernate.domain;

import static lombok.AccessLevel.PROTECTED;

import java.io.Serializable;
import java.time.LocalDateTime;
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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.cornerstone.core.domain.Auditable;
import io.cornerstone.core.domain.Ordered;
import io.cornerstone.core.domain.Treeable;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.hibernate.audit.CreationUser;
import io.cornerstone.core.hibernate.audit.UpdateUser;
import io.cornerstone.core.json.FromIdDeserializer;
import io.cornerstone.core.json.ToIdSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractTreeableEntity<T extends AbstractTreeableEntity<T>> extends AbstractPersistable<Long>
		implements Auditable, Treeable<T, Long>, Ordered<T>, Serializable {

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
	@JoinColumn(foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	@JsonView(View.Edit.class)
	@JsonSerialize(using = ToIdSerializer.class)
	@JsonDeserialize(using = FromIdDeserializer.class)
	@ApiModelProperty(dataType = "java.lang.Long", example = "1")
	protected T parent;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "parent")
	@OrderBy("displayOrder,name")
	protected Collection<T> children;

	@CreationTimestamp
	@JsonView(Auditable.class)
	@Column(updatable = false)
	private LocalDateTime createdDate;

	@UpdateTimestamp
	@JsonView(Auditable.class)
	@Column(insertable = false)
	private LocalDateTime lastModifiedDate;

	@CreationUser
	@JsonView(Auditable.class)
	@Column(updatable = false)
	private String createdBy;

	@UpdateUser
	@JsonView(Auditable.class)
	@Column(insertable = false)
	private String lastModifiedBy;

}
