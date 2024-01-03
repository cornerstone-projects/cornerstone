package io.cornerstone.core.persistence.convert;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StringArrayConverter extends AbstractArrayConverter<String>
		implements AttributeConverter<String[], String> {

	@Override
	protected String convert(String s) {
		return s;
	}

}
