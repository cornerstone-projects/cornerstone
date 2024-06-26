package io.cornerstone.core.event;

import java.util.Objects;

import io.cornerstone.core.Application;
import lombok.Getter;

import org.springframework.context.ApplicationEvent;

@Getter
public class BaseEvent<T> extends ApplicationEvent {

	private static final long serialVersionUID = -2892858943541156897L;

	private final String instanceId = currentInstanceId();

	protected final T source;

	public BaseEvent(T source) {
		super(source);
		this.source = source;
	}

	public boolean isLocal() {
		return Objects.equals(this.instanceId, currentInstanceId());
	}

	private static String currentInstanceId() {
		return Application.current().map(a -> a.getInstanceId(true)).orElse(null);
	}

	@Override
	public boolean equals(Object that) {
		if (that == null) {
			return false;
		}
		if (this == that) {
			return true;
		}
		if (!getClass().isInstance(that)) {
			return false;
		}
		BaseEvent<?> be = (BaseEvent<?>) that;
		// EventObject.source is transient
		return Objects.equals(this.instanceId, be.instanceId) && Objects.equals(this.source, be.source)
				&& Objects.equals(this.getTimestamp(), be.getTimestamp());
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		if (!"".equals(this.source)) {
			sb.append("[source=").append(this.source).append("]");
		}
		if (this.instanceId != null) {
			sb.append(" from ").append(this.instanceId);
		}
		return sb.toString();
	}

}
