
package io.cornerstone.core.persistence.type;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
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
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.engine.jdbc.internal.StreamBackedBinaryStream;
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
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.BeanUtils;

@SuppressWarnings("removal")
@Slf4j
public class JsonType extends BaseUserTypeSupport<Object> implements DynamicParameterizedType {

	private Type type;

	@Override
	public void setParameterValues(Properties parameters) {
		Object xProperty = parameters.get(DynamicParameterizedType.XPROPERTY);
		try {
			Class<?> jdkFieldDetails = Class.forName("org.hibernate.models.internal.jdk.JdkFieldDetails");
			if (jdkFieldDetails.isInstance(xProperty)) {
				this.type = ((Field) jdkFieldDetails.getMethod("toJavaMember").invoke(xProperty)).getGenericType();
			}
		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		if (this.type == null) {
			this.type = ((DynamicParameterizedType.ParameterType) parameters
				.get(DynamicParameterizedType.PARAMETER_TYPE)).getReturnedClass();
		}
	}

	@Override
	protected void resolve(BiConsumer<BasicJavaType<Object>, JdbcType> resolutionConsumer) {
		resolutionConsumer.accept(new JsonJavaType(this.type), VarcharJdbcType.INSTANCE);
	}

	static class JsonJavaType extends AbstractClassJavaType<Object> {

		private static final JsonMapper jsonMapper = JsonMapper.builder()
			.changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
			.build();

		private final Type propertyType;

		JsonJavaType(Type type) {

			super(Object.class, new MutableMutabilityPlan<>() {

				private static final long serialVersionUID = 1940316475848878030L;

				@Override
				protected Object deepCopyNotNull(Object value) {
					if (value instanceof Set<?> set) {
						return new LinkedHashSet<>(set);
					}
					if (value instanceof Collection<?> collection) {
						return new ArrayList<>(collection);
					}
					if (value instanceof Map<?, ?> map) {
						return new LinkedHashMap<>(map);
					}
					Object obj = BeanUtils.instantiateClass(value.getClass());
					BeanUtils.copyProperties(value, obj);
					return obj;
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
			return jsonMapper.writeValueAsString(value);
		}

		@Override
		public Object fromString(CharSequence string) {
			if (String.class == this.propertyType) {
				return string;
			}
			return JsonJavaType.jsonMapper.readValue(string.toString(), jsonMapper.constructType(this.propertyType));
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
				String stringValue = (value instanceof String s) ? s : toString(value);

				byte[] bytes = stringValue.getBytes();
				return (X) new StreamBackedBinaryStream(new ByteArrayInputStream(bytes), bytes.length);
			}
			else if (Blob.class.isAssignableFrom(type)) {
				String stringValue = (value instanceof String s) ? s : toString(value);

				final Blob blob = BlobJavaType.INSTANCE.fromString(stringValue);
				return (X) blob;
			}
			else if (Object.class.isAssignableFrom(type)) {
				String stringValue = (value instanceof String s) ? s : toString(value);
				return (X) jsonMapper.readTree(stringValue);
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
