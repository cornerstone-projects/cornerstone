package io.cornerstone.core.persistence.convert;

import java.util.Map;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StringMapConverter extends JsonConverter<Map<String, String>> {

}
