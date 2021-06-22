package com.example.demo.model;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.domain.Persistable;
import org.springframework.data.util.ProxyUtils;
import org.springframework.lang.Nullable;

@MappedSuperclass
public abstract class AbstractEntity<PK extends Serializable> implements Persistable<PK>, Serializable {

	private static final long serialVersionUID = -77383297685783927L;

	@Id
	@GeneratedValue(generator = "snowflake")
	@GenericGenerator(name = "snowflake", strategy = "com.example.demo.jpa.id.SnowflakeIdentifierGenerator")
	private @Nullable PK id;

	@Nullable
	@Override
	public PK getId() {
		return id;
	}

	protected void setId(@Nullable PK id) {
		this.id = id;
	}

	@Transient
	@Override
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

		AbstractEntity<?> that = (AbstractEntity<?>) obj;

		return null == this.getId() ? false : this.getId().equals(that.getId());
	}

	@Override
	public int hashCode() {

		int hashCode = 17;

		hashCode += null == getId() ? 0 : getId().hashCode() * 31;

		return hashCode;
	}
}
