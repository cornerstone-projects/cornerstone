package io.cornerstone.core.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.cornerstone.core.util.ReflectionUtils;
import lombok.Getter;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.annotation.AnnotationUtils;

public class JsonSanitizer {

	public static final JsonSanitizer DEFAULT_INSTANCE = new JsonSanitizer();

	private final ObjectMapper objectMapper;

	@Getter
	private final Map<BiPredicate<String, Object>, Function<String, String>> mapping;

	@Getter
	private final List<BiPredicate<String, Object>> dropping;

	private final ObjectWriter objectWriter;

	@JsonFilter("sanitizer")
	static class SanitizerMixIn {

	}

	public JsonSanitizer() {
		this(new ConcurrentHashMap<>());
		getMapping().put((s, obj) -> s.equals("password") || s.endsWith("Password") || s.endsWith("Passwords"),
				s -> "******");
	}

	public JsonSanitizer(Map<BiPredicate<String, Object>, Function<String, String>> mapping) {
		this(mapping, new CopyOnWriteArrayList<>());
	}

	public JsonSanitizer(Map<BiPredicate<String, Object>, Function<String, String>> mapping,
			List<BiPredicate<String, Object>> dropping) {
		this.mapping = mapping;
		this.dropping = dropping;
		FilterProvider filters = new SimpleFilterProvider().setDefaultFilter(new SimpleBeanPropertyFilter() {
			@Override
			public void serializeAsField(Object obj, JsonGenerator jgen, SerializerProvider provider,
					PropertyWriter writer) throws Exception {
				String name = writer.getName();
				if (include(writer) && dropping.stream().noneMatch(entry -> entry.test(name, obj))) {
					BeanWrapperImpl bw = new BeanWrapperImpl(obj);
					Optional<Function<String, String>> func = mapping.entrySet()
						.stream()
						.filter(entry -> entry.getKey().test(name, obj))
						.findFirst()
						.map(Entry::getValue);
					if (func.isPresent()) {
						Object value = bw.getPropertyValue(name);
						try {
							String newValue = func.get().apply(value != null ? String.valueOf(value) : null);
							Class<?> type = bw.getPropertyType(name);
							if (isNumeric(type) && isNumber(newValue)) {
								jgen.writeFieldName(name);
								jgen.writeNumber(newValue);
							}
							else if (((type == Boolean.class) || (type == boolean.class))
									&& ("true".equals(newValue) || "false".equals(newValue))) {
								jgen.writeBooleanField(name, Boolean.getBoolean(newValue));
							}
							else {
								jgen.writeStringField(name, newValue);
							}
						}
						catch (Exception ignore) {
						}
					}
					else {
						JsonSanitize annotation = null;
						try {
							annotation = AnnotationUtils.findAnnotation(bw.getPropertyDescriptor(name).getReadMethod(),
									JsonSanitize.class);
							if (annotation == null) {
								annotation = AnnotationUtils
									.findAnnotation(ReflectionUtils.getField(obj.getClass(), name), JsonSanitize.class);
							}
						}
						catch (Exception ignore) {

						}
						if (annotation == null) {
							writer.serializeAsField(obj, jgen, provider);
							return;
						}

						String newValue = annotation.value();
						if (newValue.equals(JsonSanitize.DEFAULT_NONE)) {
							writer.serializeAsOmittedField(obj, jgen, provider);
						}
						else {
							Class<?> type = bw.getPropertyType(name);
							if (isNumeric(type) && isNumber(newValue)) {
								jgen.writeFieldName(name);
								jgen.writeNumber(newValue);
							}
							else if (((type == Boolean.class) || (type == boolean.class))
									&& ("true".equals(newValue) || "false".equals(newValue))) {
								jgen.writeBooleanField(name, Boolean.getBoolean(newValue));
							}
							else {
								Object value = bw.getPropertyValue(name);
								jgen.writeStringField(name, sanitizeString(value != null ? value.toString() : null,
										newValue, annotation.position()));
							}
						}
					}
				}
				else if (!jgen.canOmitFields()) {
					writer.serializeAsOmittedField(obj, jgen, provider);
				}
			}
		}).setFailOnUnknownId(false);
		JsonMapper.Builder builder = JsonMapper.builder();
		builder.serializationInclusion(JsonInclude.Include.NON_NULL);
		builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		builder.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		builder.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		builder.enable(SerializationFeature.INDENT_OUTPUT);
		builder.enable(MapperFeature.USE_STD_BEAN_NAMING);
		builder.defaultTimeZone(TimeZone.getDefault());
		this.objectMapper = builder.build();
		this.objectWriter = this.objectMapper.addMixIn(Object.class, SanitizerMixIn.class).writer(filters);
	}

	private static String sanitizeString(String value, String mask, int position) {
		if (value != null && position >= 0) {
			int length = value.length();
			if (length > position) {
				StringBuilder sb = new StringBuilder();
				sb.append(value, 0, position);
				sb.append(mask);
				if (length > position + mask.length()) {
					sb.append(value.substring(position + mask.length()));
				}
				mask = sb.toString();
			}
		}
		return mask;
	}

	public String sanitizeValue(Object data, JsonSanitize config) {
		if (config != null) {
			if (data instanceof String) {
				data = sanitizeString((String) data, config.value(), config.position());
			}
			else if (org.springframework.beans.BeanUtils.isSimpleValueType(data.getClass())) {
				data = !config.value().equals(JsonSanitize.DEFAULT_NONE) ? config.value() : null;
			}
		}
		return toJson(data);
	}

	public String sanitizeArray(Object[] data, JsonSanitize[] config) {
		if (data == null) {
			return null;
		}
		if (data.length == 0) {
			return "[]";
		}
		StringJoiner joiner = new StringJoiner(", ", "[ ", " ]");
		for (int i = 0; i < data.length; i++) {
			joiner.add(sanitizeValue(data[i], config != null && config.length > i ? config[i] : null));
		}
		return joiner.toString();
	}

	public String toJson(Object value) {
		try {
			return this.objectWriter.writeValueAsString(value);
		}
		catch (Exception ex) {
			return null;
		}
	}

	public String sanitize(String json) {
		try {
			JsonNode node = this.objectMapper.readTree(json);
			sanitize(null, node, null);
			return this.objectMapper.writeValueAsString(node);
		}
		catch (IOException ex) {
			return json;
		}
	}

	private void sanitize(String nodeName, JsonNode node, JsonNode parent) {
		if (node.isObject()) {
			List<String> toBeDropped = new ArrayList<>();
			node.fieldNames().forEachRemaining(name -> {
				if (this.dropping.stream().anyMatch(p -> p.test(name, node))) {
					toBeDropped.add(name);
				}
			});
			((ObjectNode) node).remove(toBeDropped);
			Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
			while (iterator.hasNext()) {
				Map.Entry<String, JsonNode> entry = iterator.next();
				sanitize(entry.getKey(), entry.getValue(), node);
			}
		}
		else if (node.isArray()) {
			Iterator<JsonNode> iterator = node.elements();
			while (iterator.hasNext()) {
				JsonNode element = iterator.next();
				sanitize(null, element, node);
			}
		}
		else if (parent instanceof ObjectNode on) {
			this.mapping.forEach((k, v) -> {
				if (k.test(nodeName, parent)) {
					String value = v.apply(node.isNull() ? null : node.asText());
					try {
						if (node.isNumber()) {
							on.put(nodeName, new BigDecimal(value));
						}
						else if (node.isBoolean()) {
							on.put(nodeName, Boolean.valueOf(value));
						}
						else {
							on.put(nodeName, value);
						}
					}
					catch (Exception ex) {
						on.put(nodeName, value);
					}
				}
			});
		}
	}

	private static boolean isNumeric(Class<?> type) {
		return (short.class == type) || (int.class == type) || (long.class == type) || (float.class == type)
				|| (double.class == type) || (Number.class.isAssignableFrom(type));
	}

	private static boolean isNumber(String value) {
		try {
			Double.parseDouble(value);
			return true;
		}
		catch (Exception ex) {
			return false;
		}
	}

}
