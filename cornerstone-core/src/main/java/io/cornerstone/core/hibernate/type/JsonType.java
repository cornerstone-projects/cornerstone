
package io.cornerstone.core.hibernate.type;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.engine.jdbc.internal.BinaryStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.java.BasicJavaType;
import org.hibernate.type.descriptor.java.BlobJavaType;
import org.hibernate.type.descriptor.java.DataHelper;
import org.hibernate.type.descriptor.java.MutableMutabilityPlan;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;
import org.hibernate.usertype.BaseUserTypeSupport;
import org.hibernate.usertype.DynamicParameterizedType;

import org.springframework.beans.BeanUtils;

@Slf4j
public class JsonType extends BaseUserTypeSupport<Object> implements DynamicParameterizedType {

	private Type type;

	@Override
	public void setParameterValues(Properties parameters) {
		Object xProperty = parameters.get(DynamicParameterizedType.XPROPERTY);
		try {
			Class<?> javaXMember = Class.forName("org.hibernate.annotations.common.reflection.java.JavaXMember");
			if (javaXMember.isInstance(xProperty)) {
				this.type = (Type) javaXMember.getMethod("getJavaType").invoke(xProperty);
			}
		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		if (this.type == null) {
			this.type = ((ParameterType) parameters.get(PARAMETER_TYPE)).getReturnedClass();
		}
	}

	@Override
	protected void resolve(BiConsumer<BasicJavaType<Object>, JdbcType> resolutionConsumer) {
		resolutionConsumer.accept(new JsonJavaType(this.type), VarcharJdbcType.INSTANCE);
	}

	static class JsonJavaType extends AbstractClassJavaType<Object> {

		private static final ObjectMapper objectMapper = new ObjectMapper()
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		private final Type propertyType;

		JsonJavaType(Type type) {

			super(Object.class, new MutableMutabilityPlan<>() {

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
					}
					catch (Exception ex) {
						throw new RuntimeException(ex);
					}

				}
			});

			this.propertyType = type;
		}

		@Override
		public boolean areEqual(Object one, Object another) {
			if (one == another) {
				return true;
			}
			if (one == null || another == null) {
				return false;
			}
			if (one instanceof String && another instanceof String) {
				return one.equals(another);
			}
			if ((one instanceof Collection && another instanceof Collection)
					|| (one instanceof Map && another instanceof Map)) {
				return Objects.equals(one, another);
			}
			if (one.getClass().equals(another.getClass())) {
				return one.equals(another);
			}
			return false;
		}

		@Override
		public String toString(Object value) {
			try {
				return objectMapper.writeValueAsString(value);
			}
			catch (JsonProcessingException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public Object fromString(CharSequence string) {
			if (String.class == this.propertyType) {
				return string;
			}
			try {
				return JsonJavaType.objectMapper.readValue(string.toString(),
						objectMapper.constructType(this.propertyType));
			}
			catch (JsonProcessingException ex) {
				throw new RuntimeException(ex);
			}
		}

		@SuppressWarnings({ "unchecked" })
		@Override
		public <X> X unwrap(Object value, Class<X> type, WrapperOptions options) {
			if (value == null) {
				return null;
			}

			if (String.class.isAssignableFrom(type)) {
				return value instanceof String ? (X) value : (X) toString(value);
			}
			else if (BinaryStream.class.isAssignableFrom(type) || byte[].class.isAssignableFrom(type)) {
				String stringValue = (value instanceof String) ? (String) value : toString(value);

				return (X) new BinaryStreamImpl(
						DataHelper.extractBytes(new ByteArrayInputStream(stringValue.getBytes())));
			}
			else if (Blob.class.isAssignableFrom(type)) {
				String stringValue = (value instanceof String) ? (String) value : toString(value);

				final Blob blob = BlobJavaType.INSTANCE.fromString(stringValue);
				return (X) blob;
			}
			else if (Object.class.isAssignableFrom(type)) {
				String stringValue = (value instanceof String) ? (String) value : toString(value);
				try {
					return (X) objectMapper.readTree(stringValue);
				}
				catch (JsonProcessingException ex) {
					throw new RuntimeException(ex);
				}
			}

			throw unknownUnwrap(type);
		}

		@Override
		public <X> Object wrap(X value, WrapperOptions options) {
			if (value == null) {
				return null;
			}

			Blob blob = null;

			if (Blob.class.isAssignableFrom(value.getClass())) {
				blob = options.getLobCreator().wrap((Blob) value);
			}
			else if (byte[].class.isAssignableFrom(value.getClass())) {
				blob = options.getLobCreator().createBlob((byte[]) value);
			}
			else if (InputStream.class.isAssignableFrom(value.getClass())) {
				InputStream inputStream = (InputStream) value;
				try {
					blob = options.getLobCreator().createBlob(inputStream, inputStream.available());
				}
				catch (IOException ex) {
					throw unknownWrap(value.getClass());
				}
			}

			String stringValue;
			try {
				stringValue = (blob != null) ? new String(DataHelper.extractBytes(blob.getBinaryStream()))
						: value.toString();
			}
			catch (SQLException ex) {
				throw new HibernateException("Unable to extract binary stream from Blob", ex);
			}
			return fromString(stringValue);
		}

	}

}
