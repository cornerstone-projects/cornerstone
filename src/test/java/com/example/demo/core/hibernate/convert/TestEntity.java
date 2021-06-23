package com.example.demo.core.hibernate.convert;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.persistence.Entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TestEntity extends AbstractPersistable<Long> {

	private String[] stringArray;

	private Set<String> stringSet;

	private List<String> stringList;

	private Map<String, String> stringMap;

	private Integer[] integerArray;

	private Set<Integer> integerSet;

	private List<Integer> integerList;

	private Long[] longArray;

	private Set<Long> longSet;

	private List<Long> longList;

	private TestEnum[] enumArray;

	private Set<TestEnum> enumSet;

	private List<TestEnum> enumList;

	private List<TestComponent> testComponentList;

	@Converter(autoApply = true)
	public static class TestEnumArrayConverter extends EnumArrayConverter<TestEnum>
			implements AttributeConverter<TestEnum[], String> {
	}

	@Converter(autoApply = true)
	public static class TestEnumSetConverter extends EnumSetConverter<TestEnum> {
	}

	@Converter(autoApply = true)
	public static class TestEnumListConverter extends EnumListConverter<TestEnum> {
	}

	@Converter(autoApply = true)
	public static class TestComponentListConverter extends JsonConverter<List<TestComponent>> {
	}

}