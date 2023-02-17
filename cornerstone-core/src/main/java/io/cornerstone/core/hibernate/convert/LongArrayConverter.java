package io.cornerstone.core.hibernate.convert;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LongArrayConverter extends AbstractArrayConverter<Long> implements AttributeConverter<Long[], String> {

	@Override
	protected Long convert(String s) {
		return Long.valueOf(s);
	}

}
