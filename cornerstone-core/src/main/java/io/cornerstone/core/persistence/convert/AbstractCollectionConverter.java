package io.cornerstone.core.persistence.convert;

import java.util.Collection;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;

import org.springframework.core.ResolvableType;
import org.springframework.core.convert.support.DefaultConversionService;

@SuppressWarnings("unchecked")
public abstract class AbstractCollectionConverter<C extends Collection<T>, T> implements AttributeConverter<C, String> {

	public static final String SEPARATOR = ",";

	private final Class<T> componentType;

	public AbstractCollectionConverter() {
		this.componentType = (Class<T>) ResolvableType.forClass(getClass())
			.as(AbstractCollectionConverter.class)
			.getGeneric(1)
			.resolve();
	}

	@Override
	public String convertToDatabaseColumn(C collection) {
		if (collection == null) {
			return null;
		}
		if (collection.isEmpty()) {
			return "";
		}
		return collection.stream().map(Object::toString).collect(Collectors.joining(SEPARATOR));
	}

	@Override
	public C convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		String[] arr = string.split(SEPARATOR + "\\s*");
		C collection = createCollection();
		for (String s : arr) {
			if (!s.isEmpty()) {
				collection.add(convert(s));
			}
		}
		return collection;
	}

	protected T convert(String s) {
		return DefaultConversionService.getSharedInstance().convert(s, this.componentType);
	}

	protected abstract C createCollection();

}
