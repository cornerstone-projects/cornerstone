package io.cornerstone.core.json;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.support.DefaultConversionService;

public class FromIdDeserializer extends StdDeserializer<Object> implements ContextualDeserializer {

	private static final long serialVersionUID = 2685701643083493128L;

	private JavaType type;

	public FromIdDeserializer() {
		this((Class<Object>) null);
	}

	public FromIdDeserializer(Class<Object> t) {
		super(t);
	}

	public FromIdDeserializer(JavaType type) {
		super((Class<Object>) null);
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
						coll.add(convert(parser.getText(), componentType));
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
					obj = convert(parser.getText(), this.type);
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
	public JsonDeserializer<Object> createContextual(DeserializationContext ctx, BeanProperty beanProperty)
			throws JsonMappingException {
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
