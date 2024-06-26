package io.cornerstone.core.persistence.event;

import io.cornerstone.core.event.BaseEvent;
import lombok.Getter;

import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;
import org.springframework.data.domain.Persistable;

@Getter
public class EntityOperationEvent<T extends Persistable<?>> extends BaseEvent<T> implements ResolvableTypeProvider {

	private static final long serialVersionUID = -3336231774669978161L;

	private final EntityOperationType type;

	public EntityOperationEvent(T entity, EntityOperationType type) {
		super(entity);
		this.type = type;
	}

	public T getEntity() {
		return getSource();
	}

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(getSource()));
	}

	@Override
	public String toString() {
		return getClass().getName() + "[type=" + this.type + ", source=" + this.source.getClass().getName() + "("
				+ this.source + ")]";
	}

}
