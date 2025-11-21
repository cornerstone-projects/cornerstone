package io.cornerstone.core.json;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import org.springframework.beans.BeanWrapperImpl;

public class ToIdSerializer extends StdSerializer<Object> {

	public ToIdSerializer() {
		this(null);
	}

	public ToIdSerializer(Class<Object> t) {
		super(t);
	}

	@Override
	public void serialize(Object obj, JsonGenerator generator, SerializationContext context) {
		if (obj instanceof Collection<?> coll) {
			List<Object> ids = coll.stream()
				.map(Optional::ofNullable)
				.map(o -> o.map(o2 -> new BeanWrapperImpl(o2).getPropertyValue("id")).orElse(null))
				.toList();
			generator.writePOJO(ids);
		}
		else if (obj instanceof Object[] array) {
			List<Object> ids = Stream.of(array)
				.map(Optional::ofNullable)
				.map(o -> o.map(o2 -> new BeanWrapperImpl(o2).getPropertyValue("id")).orElse(null))
				.toList();
			generator.writePOJO(ids);
		}
		else {
			Object id = obj != null ? new BeanWrapperImpl(obj).getPropertyValue("id") : null;
			if (id == null) {
				JsonInclude.Include include = context.getConfig().getDefaultPropertyInclusion().getValueInclusion();
				if (((include == JsonInclude.Include.NON_NULL) || (include == JsonInclude.Include.NON_EMPTY))) {
					generator.writeNull();
					// how to skip current field ?
				}
				else {
					generator.writeNull();
				}
			}
			else {
				if (id instanceof String stringId) {
					generator.writeRawValue(stringId);
				}
				else if (id instanceof Number numberId) {
					generator.writeNumber(String.valueOf(numberId));
				}
				else {
					generator.writePOJO(id);
				}
			}
		}
	}

}
