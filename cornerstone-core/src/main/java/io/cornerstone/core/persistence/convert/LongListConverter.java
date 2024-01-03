package io.cornerstone.core.persistence.convert;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LongListConverter extends AbstractListConverter<Long> {

	@Override
	protected Long convert(String s) {
		return Long.valueOf(s);
	}

}
