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
import lombok.experimental.UtilityClass;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.DefaultBaseTypeLimitingValidator;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

import org.springframework.cache.support.NullValue;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.jackson.SimpleGrantedAuthorityMixin;
import org.springframework.util.ClassUtils;

@UtilityClass
public class JsonSerializationUtils {

	private static final JsonMapper defaultTypingObjectMapper = createJsonMapperBuilder()
		.activateDefaultTyping(new DefaultBaseTypeLimitingValidator() {
			@Override
			public Validity validateBaseType(DatabindContext ctx, JavaType baseType) {
				if (baseType.getRawClass() == Object.class) {
					return Validity.ALLOWED;
				}
				return super.validateBaseType(ctx, baseType);
			}
		}, DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
		.build();

	public static byte[] serialize(Object object) throws IOException {
		if (object == null) {
			return null;
		}
		return switch (object) {
			case Long l -> (object + "L").getBytes();
			case Float v -> (object + "F").getBytes();
			case Enum<?> en -> (object.getClass().getName() + '.' + en.name()).getBytes();
			default -> defaultTypingObjectMapper.writeValueAsBytes(object);
		};
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

	public static JsonMapper.Builder createJsonMapperBuilder() {
		JsonMapper.Builder builder = JsonMapper.builder()
			.changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
			.addMixIn(Throwable.class, ThrowableMixin.class)
			.addMixIn(GrantedAuthority.class, SimpleGrantedAuthorityMixin.class)
			.addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityMixin.class)
			.addModule(new SimpleModule().addSerializer(new NullValueSerializer())
				.addDeserializer(NullValue.class, new ValueDeserializer<>() {
					@Override
					public NullValue deserialize(JsonParser jsonparser, DeserializationContext deserializationcontext) {
						return (NullValue) NullValue.INSTANCE;
					}
				}))
			.annotationIntrospector(SmartJacksonAnnotationIntrospector.INSTANCE);

		if (ClassUtils.isPresent("tools.jackson.module.mrbean.MrBeanModule",
				JsonSerializationUtils.class.getClassLoader())) {
			builder.addModule(new tools.jackson.module.mrbean.MrBeanModule());
		}
		return builder;
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
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

		private final Map<Member, Boolean> cache = new ConcurrentHashMap<>(1024);

		private SmartJacksonAnnotationIntrospector() {

		}

		@Override
		public boolean hasIgnoreMarker(MapperConfig<?> config, AnnotatedMember m) {
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
				if (mem instanceof Method method) {
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

		NullValueSerializer() {
			super(NullValue.class);
		}

		@Override
		public void serialize(NullValue value, JsonGenerator jgen, SerializationContext context) {
			jgen.writeStartObject();
			jgen.writeStringProperty("@class", NullValue.class.getName());
			jgen.writeEndObject();
		}

	}

}
