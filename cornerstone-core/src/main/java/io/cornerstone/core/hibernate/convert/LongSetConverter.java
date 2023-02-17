package io.cornerstone.core.hibernate.convert;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LongSetConverter extends AbstractSetConverter<Long> {

	@Override
	protected Long convert(String s) {
		return Long.valueOf(s);
	}

}
