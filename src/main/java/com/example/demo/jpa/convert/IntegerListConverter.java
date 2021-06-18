package com.example.demo.jpa.convert;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class IntegerListConverter extends AbstractListConverter<Integer> {

	@Override
	protected Integer convert(String s) {
		return Integer.valueOf(s);
	}

}