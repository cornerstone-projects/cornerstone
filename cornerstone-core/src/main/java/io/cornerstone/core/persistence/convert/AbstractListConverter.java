package io.cornerstone.core.persistence.convert;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractListConverter<T> extends AbstractCollectionConverter<List<T>, T> {

	@Override
	protected List<T> createCollection() {
		return new ArrayList<>();
	}

}
