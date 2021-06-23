package com.example.demo.core.hibernate.type;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.annotations.common.reflection.java.JavaXMember;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.MutableMutabilityPlan;
import org.hibernate.usertype.DynamicParameterizedType;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonTypeDescriptor extends AbstractTypeDescriptor<Object> implements DynamicParameterizedType {

	private static final long serialVersionUID = -3758905487686034882L;

	private final static ObjectMapper objectMapper = new ObjectMapper()
			.setSerializationInclusion(JsonInclude.Include.NON_NULL).disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

	private Type type;

	@Override
	public void setParameterValues(Properties parameters) {
		final XProperty xProperty = (XProperty) parameters.get(DynamicParameterizedType.XPROPERTY);
		if (xProperty instanceof JavaXMember) {
			type = ((JavaXMember) xProperty).getJavaType();
		} else {
			type = ((ParameterType) parameters.get(PARAMETER_TYPE)).getReturnedClass();
		}
	}

	public JsonTypeDescriptor() {
		super(Object.class, new MutableMutabilityPlan<Object>() {

			private static final long serialVersionUID = 1940316475848878030L;

			@Override
			protected Object deepCopyNotNull(Object value) {
				if (value instanceof Set) {
					return new LinkedHashSet<>((Set<?>) value);
				}
				if (value instanceof Collection) {
					return new ArrayList<>((Collection<?>) value);
				}
				if (value instanceof Map) {
					return new LinkedHashMap<>((Map<?, ?>) value);
				}
				Object obj;
				try {
					obj = BeanUtils.instantiateClass(value.getClass());
					BeanUtils.copyProperties(value, obj);
					return obj;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			}
		});
	}

	@Override
	public String toString(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new IllegalArgumentException(value + " cannot be serialized as json ", e);
		}
	}

	@Override
	public Object fromString(String string) {
		if (string == null)
			return null;
		try {
			return objectMapper.readValue(string, objectMapper.constructType(type));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <X> X unwrap(Object value, Class<X> type, WrapperOptions options) {
		if (value == null)
			return null;
		if (String.class == type)
			return (X) toString(value);
		throw unknownUnwrap(type);
	}

	@Override
	public <X> Object wrap(X value, WrapperOptions options) {
		if (value == null)
			return null;
		return fromString(value.toString());
	}

}
