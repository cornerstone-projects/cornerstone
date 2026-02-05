package io.cornerstone.core.json;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.support.DefaultConversionService;

public class FromIdDeserializer extends ValueDeserializer<Object> {

	private final JavaType type;

	public FromIdDeserializer() {
		this.type = null;
	}

	public FromIdDeserializer(JavaType type) {
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object deserialize(JsonParser parser, DeserializationContext ctx) {
		if (this.type == null) {
			return null;
		}
		try {
			if (this.type.isCollectionLikeType() || this.type.isArrayType()) {
				Collection<Object> coll;
				JavaType componentType = this.type.getContentType();
				if (this.type.isArrayType()) {
					coll = new ArrayList<>();
				}
				else {
					Class<?> clazz = this.type.getRawClass();
					if (this.type.isConcrete()) {
						coll = (Collection<Object>) BeanUtils.instantiateClass(clazz);
					}
					else if (clazz.isAssignableFrom(ArrayList.class)) {
						coll = new ArrayList<>();
					}
					else {
						coll = new LinkedHashSet<>();
					}
				}
				if (parser.currentToken() != JsonToken.START_ARRAY) {
					throw new RuntimeException("Not array node");
				}
				while (parser.nextToken() != JsonToken.END_ARRAY) {
					if (Objects.requireNonNull(parser.currentToken()) == JsonToken.START_OBJECT) {
						coll.add(parser.readValueAs(componentType.getRawClass()));
					}
					else {
						coll.add(convert(parser.getString(), componentType));
					}
				}
				if (this.type.isArrayType()) {
					List<Object> list = (List<Object>) coll;
					Object array = Array.newInstance(componentType.getRawClass(), list.size());
					for (int i = 0; i < list.size(); i++) {
						Array.set(array, i, list.get(i));
					}
					return array;
				}
				else {
					return coll;
				}
			}
			else if (this.type.isConcrete()) {
				Object obj;
				if (!parser.currentToken().isScalarValue()) {
					obj = parser.readValueAs(this.type.getRawClass());
				}
				else {
					obj = convert(parser.getString(), this.type);
				}
				return obj;
			}
			else {
				throw new RuntimeException("cannot deserialize " + this.type);
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty beanProperty) {
		return new FromIdDeserializer(beanProperty.getType());
	}

	private static Object convert(String id, JavaType type) throws Exception {
		Class<?> clazz = type.getRawClass();
		Method setId = BeanUtils.findMethodWithMinimalParameters(clazz, "setId");
		if (setId == null) {
			return null;
		}
		setId.setAccessible(true);
		Object object = BeanUtils.instantiateClass(clazz);
		setId.invoke(object, DefaultConversionService.getSharedInstance().convert(id, setId.getParameterTypes()[0]));
		return object;
	}

}
