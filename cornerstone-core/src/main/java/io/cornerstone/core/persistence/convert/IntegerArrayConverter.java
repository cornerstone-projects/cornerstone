package io.cornerstone.core.persistence.convert;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class IntegerArrayConverter extends AbstractArrayConverter<Integer>
		implements AttributeConverter<Integer[], String> {

}
