package io.cornerstone.core.util;

import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.core.util.JsonUtils;

import org.springframework.cache.support.NullValue;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.jackson2.SimpleGrantedAuthorityMixin;
import org.springframework.util.ClassUtils;

@UtilityClass
public class JsonSerializationUtils {

	private static final ObjectMapper defaultTypingObjectMapper = createObjectMapper();
	static {
		defaultTypingObjectMapper.activateDefaultTyping(defaultTypingObjectMapper.getPolymorphicTypeValidator(),
				ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
	}

	public static byte[] serialize(Object object) throws IOException {
		if (object == null) {
			return null;
		}
		if (object instanceof Long) {
			return (object + "L").getBytes();
		}
		else if (object instanceof Float) {
			return (object + "F").getBytes();
		}
		else if (object instanceof Enum) {
			return (object.getClass().getName() + '.' + ((Enum<?>) object).name()).getBytes();
		}
		return defaultTypingObjectMapper.writeValueAsBytes(object);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object deserialize(byte[] bytes) throws IOException {
		if (bytes == null) {
			return null;
		}
		if (Character.isDigit((char) bytes[0])) {
			if (((char) bytes[bytes.length - 1]) == 'L') {
				return Long.valueOf(new String(bytes, 0, bytes.length - 1));
			}
			else if (((char) bytes[bytes.length - 1]) == 'F') {
				return Float.valueOf(new String(bytes, 0, bytes.length - 1));
			}
		}
		if (Character.isAlphabetic((char) bytes[0]) && Character.isAlphabetic((char) bytes[bytes.length - 1])) {
			String string = new String(bytes);
			int index = string.lastIndexOf('.');
			if (index > 0) {
				String clazz = string.substring(0, index);
				String name = string.substring(index + 1);
				try {
					return Enum.valueOf((Class<Enum>) Class.forName(clazz), name);
				}
				catch (ClassNotFoundException ex) {
					return null;
				}
			}
		}
		return defaultTypingObjectMapper.readValue(bytes, Object.class);
	}

	public static ObjectMapper createObjectMapper() {
		return createObjectMapper(null);
	}

	public static ObjectMapper createObjectMapper(JsonFactory jsonFactory) {
		ObjectMapper objectMapper = new ObjectMapper(jsonFactory)
			.setSerializationInclusion(JsonInclude.Include.NON_NULL)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
			.addMixIn(Throwable.class, ThrowableMixin.class)
			.addMixIn(GrantedAuthority.class, SimpleGrantedAuthorityMixin.class)
			.addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityMixin.class)
			.registerModule(new SimpleModule().addSerializer(new NullValueSerializer())
				.addDeserializer(NullValue.class, new JsonDeserializer<NullValue>() {
					@Override
					public NullValue deserialize(JsonParser jsonparser, DeserializationContext deserializationcontext)
							throws IOException, JsonProcessingException {
						return (NullValue) NullValue.INSTANCE;
					}
				}))
			.setAnnotationIntrospector(SmartJacksonAnnotationIntrospector.INSTANCE);
		if (ClassUtils.isPresent("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule",
				JsonUtils.class.getClassLoader())) {
			objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
		}
		if (ClassUtils.isPresent("com.fasterxml.jackson.module.paramnames.ParameterNamesModule",
				JsonUtils.class.getClassLoader())) {
			objectMapper.registerModule(new com.fasterxml.jackson.module.paramnames.ParameterNamesModule());
		}
		if (ClassUtils.isPresent("com.fasterxml.jackson.module.mrbean.MrBeanModule",
				JsonUtils.class.getClassLoader())) {
			objectMapper.registerModule(new com.fasterxml.jackson.module.mrbean.MrBeanModule());
		}
		return objectMapper;
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
			getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
			isGetterVisibility = JsonAutoDetect.Visibility.NONE)
	@JsonIgnoreProperties(value = { "localizedMessage", "cause", "suppressed" }, ignoreUnknown = true)
	public abstract static class ThrowableMixin {

		@JsonCreator
		public ThrowableMixin(@JsonProperty("message") String message) {
		}

	}

	static final class SmartJacksonAnnotationIntrospector extends JacksonAnnotationIntrospector {

		public static final SmartJacksonAnnotationIntrospector INSTANCE = new SmartJacksonAnnotationIntrospector();

		private static final long serialVersionUID = 412899061519525960L;

		private final Map<Member, Boolean> cache = new ConcurrentHashMap<>(1024);

		private SmartJacksonAnnotationIntrospector() {

		}

		@Override
		public boolean hasIgnoreMarker(AnnotatedMember m) {
			Member member = m.getMember();
			Class<?> declaringClass = member.getDeclaringClass();
			if (GrantedAuthority.class.isAssignableFrom(declaringClass)
					|| declaringClass.getName().startsWith("java.")) {
				return false;
			}
			Boolean bool = this.cache.get(member);
			if (bool != null) {
				return bool;
			}
			return this.cache.computeIfAbsent(member, mem -> {
				if (mem instanceof Method) {
					Method method = (Method) mem;
					String name = method.getName();
					if (name.startsWith("get") || name.startsWith("is")) {
						name = name.startsWith("get") ? 's' + name.substring(1) : "set" + name.substring(2);
						try {
							declaringClass.getMethod(name, method.getReturnType());
							return false;
						}
						catch (NoSuchMethodException ex) {
							boolean hasOtherSetter = false;
							for (Method met : declaringClass.getMethods()) {
								if (met.getName().startsWith("set") && met.getReturnType() == void.class
										&& met.getParameterTypes().length == 1) {
									hasOtherSetter = true;
									break;
								}
							}
							return hasOtherSetter;
						}
					}
				}
				int modifier = mem.getModifiers();
				return Modifier.isTransient(modifier) || Modifier.isStatic(modifier);
			});
		}

	}

	private static class NullValueSerializer extends StdSerializer<NullValue> {

		private static final long serialVersionUID = 1999052150548658808L;

		private final String classIdentifier = "@class";

		NullValueSerializer() {
			super(NullValue.class);
		}

		@Override
		public void serialize(NullValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			jgen.writeStartObject();
			jgen.writeStringField(this.classIdentifier, NullValue.class.getName());
			jgen.writeEndObject();
		}

	}

}
