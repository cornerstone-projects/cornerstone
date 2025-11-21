package io.cornerstone.core.json;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import io.cornerstone.core.util.ReflectionUtils;
import lombok.Getter;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.ser.FilterProvider;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.annotation.AnnotationUtils;

public class JsonSanitizer {

	public static final JsonSanitizer DEFAULT_INSTANCE = new JsonSanitizer();

	private final JsonMapper jsonMapper;

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
			public void serializeAsProperty(Object obj, JsonGenerator jgen, SerializationContext context,
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
								jgen.writeName(name);
								jgen.writeNumber(newValue);
							}
							else if (((type == Boolean.class) || (type == boolean.class))
									&& ("true".equals(newValue) || "false".equals(newValue))) {
								jgen.writeBooleanProperty(name, Boolean.getBoolean(newValue));
							}
							else {
								jgen.writeStringProperty(name, newValue);
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
							writer.serializeAsProperty(obj, jgen, context);
							return;
						}

						String newValue = annotation.value();
						if (newValue.equals(JsonSanitize.DEFAULT_NONE)) {
							writer.serializeAsOmittedProperty(obj, jgen, context);
						}
						else {
							Class<?> type = bw.getPropertyType(name);
							if (isNumeric(type) && isNumber(newValue)) {
								jgen.writeName(name);
								jgen.writeNumber(newValue);
							}
							else if (((type == Boolean.class) || (type == boolean.class))
									&& ("true".equals(newValue) || "false".equals(newValue))) {
								jgen.writeBooleanProperty(name, Boolean.getBoolean(newValue));
							}
							else {
								Object value = bw.getPropertyValue(name);
								jgen.writeStringProperty(name, sanitizeString(value != null ? value.toString() : null,
										newValue, annotation.position()));
							}
						}
					}
				}
				else if (!jgen.canOmitProperties()) {
					writer.serializeAsOmittedProperty(obj, jgen, context);
				}
			}
		}).setFailOnUnknownId(false);
		JsonMapper.Builder builder = JsonMapper.builder();
		builder.changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL));
		builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		builder.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		builder.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS);
		builder.enable(SerializationFeature.INDENT_OUTPUT);
		builder.defaultTimeZone(TimeZone.getDefault());
		builder.addMixIn(Object.class, SanitizerMixIn.class);
		this.jsonMapper = builder.build();
		this.objectWriter = this.jsonMapper.writer(filters);
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
		JsonNode node = this.jsonMapper.readTree(json);
		sanitize(null, node, null);
		return this.jsonMapper.writeValueAsString(node);
	}

	private void sanitize(String nodeName, JsonNode node, JsonNode parent) {
		if (node.isObject()) {
			List<String> toBeDropped = new ArrayList<>();
			node.propertyNames().forEach(name -> {
				if (this.dropping.stream().anyMatch(p -> p.test(name, node))) {
					toBeDropped.add(name);
				}
			});
			((ObjectNode) node).remove(toBeDropped);
			for (Entry<String, JsonNode> entry : node.properties()) {
				sanitize(entry.getKey(), entry.getValue(), node);
			}
		}
		else if (node instanceof ArrayNode arrayNode) {
			for (JsonNode element : arrayNode) {
				sanitize(null, element, node);
			}
		}
		else if (parent instanceof ObjectNode on) {
			this.mapping.forEach((k, v) -> {
				if (k.test(nodeName, parent)) {
					String value = v.apply(node.isNull() ? null : node.asString());
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
