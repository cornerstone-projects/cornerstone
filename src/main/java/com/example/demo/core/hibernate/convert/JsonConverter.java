package com.example.demo.core.hibernate.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeConverter;

import org.springframework.core.ResolvableType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public abstract class JsonConverter<T> implements AttributeConverter<T, String> {

	private final static ObjectMapper objectMapper = new ObjectMapper()
			.setSerializationInclusion(JsonInclude.Include.NON_NULL).disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

	private final Type type;

	public JsonConverter() {
		type = ResolvableType.forClass(getClass()).as(JsonConverter.class).getGeneric(0).getType();
	}

	@Override
	public String convertToDatabaseColumn(T obj) {
		if (obj == null)
			return null;
		if (obj instanceof Collection && ((Collection<?>) obj).isEmpty()
				|| obj instanceof Map && ((Map<?, ?>) obj).isEmpty())
			return "";
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new IllegalArgumentException(obj + " cannot be serialized as json ", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T convertToEntityAttribute(String string) {
		if (string == null)
			return null;
		if (string.isEmpty()) {
			if (type instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) type;
				if (List.class.isAssignableFrom((Class<?>) pt.getRawType())) {
					return (T) new ArrayList<>();
				} else if (Set.class.isAssignableFrom((Class<?>) pt.getRawType())) {
					return (T) new LinkedHashSet<>();
				} else if (Map.class.isAssignableFrom((Class<?>) pt.getRawType())) {
					return (T) new LinkedHashMap<>();
				}
			}
			return null;
		}
		try {
			return (T) objectMapper.readValue(string, objectMapper.constructType(type));
		} catch (Exception e) {
			throw new IllegalArgumentException(string + " is not valid json ", e);
		}
	}

}