package io.cornerstone.core.persistence.convert;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractSetConverter<T> extends AbstractCollectionConverter<Set<T>, T> {

	@Override
	protected Set<T> createCollection() {
		return new LinkedHashSet<>();
	}

}
