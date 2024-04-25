package io.cornerstone.core.persistence.convert;

import java.lang.reflect.Array;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.AttributeConverter;

import org.springframework.core.GenericTypeResolver;
import org.springframework.core.convert.support.DefaultConversionService;

@SuppressWarnings("unchecked")
public abstract class AbstractArrayConverter<T> implements AttributeConverter<T[], String> {

	public static final String SEPARATOR = AbstractCollectionConverter.SEPARATOR;

	private final Class<T> componentType;

	public AbstractArrayConverter() {
		this.componentType = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(),
				AbstractArrayConverter.class);
	}

	@Override
	public String convertToDatabaseColumn(T[] array) {
		if (array == null) {
			return null;
		}
		if (array.length == 0) {
			return "";
		}
		return Stream.of(array).map(Object::toString).collect(Collectors.joining(SEPARATOR));
	}

	@Override
	public T[] convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		if (string.isEmpty()) {
			return (T[]) Array.newInstance(this.componentType, 0);
		}
		String[] arr = string.split(SEPARATOR + "\\s*");
		T[] array = (T[]) Array.newInstance(this.componentType, arr.length);
		for (int i = 0; i < arr.length; i++) {
			array[i] = convert(arr[i]);
		}
		return array;
	}

	protected T convert(String s) {
		return DefaultConversionService.getSharedInstance().convert(s, this.componentType);
	}

}
