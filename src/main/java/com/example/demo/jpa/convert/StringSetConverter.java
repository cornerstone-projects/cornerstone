package com.example.demo.jpa.convert;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class StringSetConverter extends AbstractSetConverter<String> {

	@Override
	protected String convert(String s) {
		return s;
	}

}