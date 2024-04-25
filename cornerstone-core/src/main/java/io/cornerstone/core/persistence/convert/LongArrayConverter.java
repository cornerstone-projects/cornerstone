package io.cornerstone.core.persistence.convert;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LongArrayConverter extends AbstractArrayConverter<Long> implements AttributeConverter<Long[], String> {

}
