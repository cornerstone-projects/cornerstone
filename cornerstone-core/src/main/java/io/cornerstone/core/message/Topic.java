package io.cornerstone.core.message;

import java.io.Serializable;

import io.cornerstone.core.domain.Scope;

public interface Topic<T extends Serializable> {

	void subscribe(T message);

	void publish(T message, Scope scope);

	default void publish(T message) {
		publish(message, Scope.APPLICATION);
	}

}
