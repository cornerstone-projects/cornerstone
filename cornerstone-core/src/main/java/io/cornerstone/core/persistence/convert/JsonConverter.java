package io.cornerstone.core.persistence.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.AttributeConverter;

import org.springframework.core.ResolvableType;

public abstract class JsonConverter<T> implements AttributeConverter<T, String> {

	private static final ObjectMapper objectMapper = new ObjectMapper()
		.setSerializationInclusion(JsonInclude.Include.NON_NULL)
		.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

	private final Type type;

	public JsonConverter() {
		this.type = ResolvableType.forClass(getClass()).as(JsonConverter.class).getGeneric(0).getType();
	}

	@Override
	public String convertToDatabaseColumn(T obj) {
		if (obj == null) {
			return null;
		}
		if (((obj instanceof Collection<?> coll) && coll.isEmpty())
				|| ((obj instanceof Map<?, ?> map) && map.isEmpty())) {
			return "";
		}
		try {
			return objectMapper.writeValueAsString(obj);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(obj + " cannot be serialized as json ", ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T convertToEntityAttribute(String string) {
		if (string == null || string.contentEquals("null")) {
			return null;
		}
		if (string.isEmpty()) {
			if (this.type instanceof ParameterizedType pt) {
				if (List.class.isAssignableFrom((Class<?>) pt.getRawType())) {
					return (T) new ArrayList<>();
				}
				else if (Set.class.isAssignableFrom((Class<?>) pt.getRawType())) {
					return (T) new LinkedHashSet<>();
				}
				else if (Map.class.isAssignableFrom((Class<?>) pt.getRawType())) {
					return (T) new LinkedHashMap<>();
				}
			}
			return null;
		}
		try {
			return objectMapper.readValue(string, objectMapper.constructType(this.type));
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(string + " is not valid json ", ex);
		}
	}

}
