package com.example.demo.jpa.convert;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.AttributeConverter;

import org.springframework.core.ResolvableType;

public abstract class EnumSetConverter<T extends Enum<T>> implements AttributeConverter<Set<T>, String> {

	public static final String SEPARATOR = AbstractCollectionConverter.SEPARATOR;

	private Class<T> enumType;

	@SuppressWarnings("unchecked")
	public EnumSetConverter() {
		Class<T> clazz = (Class<T>) ResolvableType.forClass(getClass()).as(EnumArrayConverter.class).resolveGeneric(0);
		if (clazz != null)
			enumType = clazz;
	}

	@Override
	public String convertToDatabaseColumn(Set<T> set) {
		if (set == null)
			return null;
		if (set.isEmpty())
			return "";
		List<String> names = new ArrayList<>();
		for (Enum<?> en : set)
			names.add(en.name());
		return String.join(SEPARATOR, names);
	}

	@Override
	public Set<T> convertToEntityAttribute(String string) {
		if (string == null)
			return null;
		if (string.isEmpty())
			return new LinkedHashSet<>();
		String[] names = string.split(SEPARATOR);
		Set<T> set = new LinkedHashSet<>();
		for (String name : names)
			set.add(Enum.valueOf(enumType, name));
		return set;
	}

}