package io.cornerstone.core.hibernate.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.MappedSuperclass;

import org.springframework.data.domain.Persistable;
import org.springframework.data.util.ProxyUtils;

@MappedSuperclass
public abstract class AbstractPersistable<ID extends Serializable> implements Persistable<ID>, Serializable {

	private static final long serialVersionUID = -2241508041793759552L;

	@JsonIgnore
	@Override
	public boolean isNew() {
		return getId() == null;
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
		ID id = this.getId();
		return id != null && id.equals(that.getId());
	}

	@Override
	public int hashCode() {
		int hashCode = 17;
		ID id = this.getId();
		hashCode += id == null ? 0 : id.hashCode() * 31;
		return hashCode;
	}

	@Override
	public String toString() {
		return String.format("Entity of type %s with id: %s", this.getClass().getName(), getId());
	}

}
