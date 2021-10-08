package io.cornerstone.core.hibernate.convert;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class AbstractCollectionConverter<T> {

	public static final String SEPARATOR = ",";

	protected static <T> String doConvertToDatabaseColumn(Collection<T> collection) {
		if (collection == null) {
			return null;
		}
		if (collection.isEmpty()) {
			return "";
		}
		return String.join(SEPARATOR, collection.stream().map(Object::toString).collect(Collectors.toList()));
	}

	protected Collection<T> doConvertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		String[] arr = string.split(SEPARATOR + "\\s*");
		Collection<T> collection = collection();
		for (String s : arr) {
			if (!s.isEmpty()) {
				collection.add(convert(s));
			}
		}
		return collection;
	}

	protected abstract Collection<T> collection();

	protected abstract T convert(String s);

}
