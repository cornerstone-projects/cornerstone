package io.cornerstone.core.hibernate.convert;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StringListConverter extends AbstractListConverter<String> {

	@Override
	protected String convert(String s) {
		return s;
	}

}
