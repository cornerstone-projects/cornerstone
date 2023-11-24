package io.cornerstone.core.hibernate.convert;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeConverter;

import org.springframework.core.GenericTypeResolver;

public abstract class EnumListConverter<T extends Enum<T>> implements AttributeConverter<List<T>, String> {

	public static final String SEPARATOR = AbstractCollectionConverter.SEPARATOR;

	private final Class<T> enumType;

	@SuppressWarnings("unchecked")
	public EnumListConverter() {
		this.enumType = (Class<T>) GenericTypeResolver.resolveTypeArgument((getClass()), EnumListConverter.class);
	}

	@Override
	public String convertToDatabaseColumn(List<T> list) {
		if (list == null) {
			return null;
		}
		if (list.isEmpty()) {
			return "";
		}
		List<String> names = new ArrayList<>();
		for (Enum<?> en : list) {
			names.add(en.name());
		}
		return String.join(SEPARATOR, names);
	}

	@Override
	public List<T> convertToEntityAttribute(String string) {
		if (string == null) {
			return null;
		}
		if (string.isEmpty()) {
			return new ArrayList<>();
		}
		String[] names = string.split(SEPARATOR);
		List<T> list = new ArrayList<>();
		for (String name : names) {
			list.add(Enum.valueOf(this.enumType, name));
		}
		return list;
	}

}
