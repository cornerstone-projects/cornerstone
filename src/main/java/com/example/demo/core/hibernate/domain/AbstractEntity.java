package com.example.demo.core.hibernate.domain;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.data.domain.Persistable;
import org.springframework.data.util.ProxyUtils;
import org.springframework.lang.Nullable;

import com.example.demo.core.hibernate.type.JsonType;
import com.fasterxml.jackson.annotation.JsonIgnore;

@TypeDefs({ @TypeDef(name = "json", typeClass = JsonType.class) })
@MappedSuperclass
public abstract class AbstractEntity implements Persistable<Long>, Serializable {

	private static final long serialVersionUID = 3494244656461491770L;

	@Id
	@GeneratedValue(generator = "snowflake")
	@GenericGenerator(name = "snowflake", strategy = "com.example.demo.core.hibernate.id.SnowflakeIdentifierGenerator")
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

	@Override
	public String toString() {
		return String.format("Entity of type %s with id: %s", this.getClass().getName(), getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (!getClass().equals(ProxyUtils.getUserClass(obj))) {
			return false;
		}
		AbstractEntity that = (AbstractEntity) obj;
		return null == this.getId() ? false : this.getId().equals(that.getId());
	}

	@Override
	public int hashCode() {
		int hashCode = 17;
		hashCode += null == getId() ? 0 : getId().hashCode() * 31;
		return hashCode;
	}
}
