package io.cornerstone.core.json;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.springframework.beans.BeanWrapperImpl;

public class ToIdSerializer extends StdSerializer<Object> {

	private static final long serialVersionUID = -7683493644667066248L;

	public ToIdSerializer() {
		this(null);
	}

	public ToIdSerializer(Class<Object> t) {
		super(t);
	}

	@Override
	public void serialize(Object obj, JsonGenerator generator, SerializerProvider sp) throws IOException {
		if (obj instanceof Collection<?> coll) {
			List<Object> ids = coll.stream()
				.map(Optional::ofNullable)
				.map(o -> o.map(o2 -> new BeanWrapperImpl(o2).getPropertyValue("id")).orElse(null))
				.toList();
			generator.writeObject(ids);
		}
		else if (obj instanceof Object[] array) {
			List<Object> ids = Stream.of(array)
				.map(Optional::ofNullable)
				.map(o -> o.map(o2 -> new BeanWrapperImpl(o2).getPropertyValue("id")).orElse(null))
				.toList();
			generator.writeObject(ids);
		}
		else {
			Object id = obj != null ? new BeanWrapperImpl(obj).getPropertyValue("id") : null;
			if (id == null) {
				JsonInclude.Include include = sp.getConfig().getDefaultPropertyInclusion().getValueInclusion();
				if (((include == JsonInclude.Include.NON_NULL) || (include == JsonInclude.Include.NON_EMPTY))) {
					generator.writeObject(null);
					// how to skip current field ?
				}
				else {
					generator.writeNull();
				}
			}
			else {
				generator.writeObject(id);
			}
		}
	}

}
