package io.cornerstone.core.persistence.convert;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LongSetConverter extends AbstractSetConverter<Long> {

}
