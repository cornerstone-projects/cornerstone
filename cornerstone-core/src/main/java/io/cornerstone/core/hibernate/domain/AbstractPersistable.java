package io.cornerstone.core.hibernate.domain;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.data.domain.Persistable;
import org.springframework.data.util.ProxyUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.cornerstone.core.hibernate.type.JsonType;

@TypeDefs({ @TypeDef(name = "json", typeClass = JsonType.class) })
@MappedSuperclass
public abstract class AbstractPersistable<ID extends Serializable> implements Persistable<ID>, Serializable {

	private static final long serialVersionUID = -2241508041793759552L;

	@JsonIgnore
	@Override
	public boolean isNew() {
		return getId() == null;
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
		AbstractPersistable<?> that = (AbstractPersistable<?>) obj;
		return this.getId() == null ? false : this.getId().equals(that.getId());
	}

	@Override
	public int hashCode() {
		int hashCode = 17;
		hashCode += getId() == null ? 0 : getId().hashCode() * 31;
		return hashCode;
	}
}
