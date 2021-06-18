package com.example.demo.jpa.convert;

import java.lang.reflect.Array;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.ResolvableType;

@SuppressWarnings("unchecked")
public abstract class AbstractArrayConverter<T> {

	public static final String SEPARATOR = AbstractCollectionConverter.SEPARATOR;

	private final Class<?> clazz;

	public AbstractArrayConverter() {
		clazz = ResolvableType.forClass(getClass()).as(AbstractArrayConverter.class).resolveGeneric(0);
	}

	public String convertToDatabaseColumn(T[] array) {
		if (array == null)
			return null;
		if (array.length == 0)
			return "";
		return String.join(SEPARATOR, Stream.of(array).map(Object::toString).collect(Collectors.toList()));
	}

	public T[] convertToEntityAttribute(String string) {
		if (string == null)
			return null;
		if (string.isEmpty())
			return (T[]) Array.newInstance(clazz, 0);
		String[] arr = string.split(SEPARATOR + "\\s*");
		T[] array = (T[]) Array.newInstance(clazz, arr.length);
		for (int i = 0; i < arr.length; i++)
			array[i] = convert(arr[i]);
		return array;
	}

	protected abstract T convert(String s);

}