package io.cornerstone.core.hibernate.converter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.persistence.Entity;

import org.springframework.data.jpa.domain.AbstractPersistable;

import io.cornerstone.core.hibernate.convert.EnumArrayConverter;
import io.cornerstone.core.hibernate.convert.EnumListConverter;
import io.cornerstone.core.hibernate.convert.EnumSetConverter;
import io.cornerstone.core.hibernate.convert.JsonConverter;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
class TestEntity extends AbstractPersistable<Long> {

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
	static class TestEnumArrayConverter extends EnumArrayConverter<TestEnum>
			implements AttributeConverter<TestEnum[], String> {
	}

	@Converter(autoApply = true)
	static class TestEnumSetConverter extends EnumSetConverter<TestEnum> {
	}

	@Converter(autoApply = true)
	static class TestEnumListConverter extends EnumListConverter<TestEnum> {
	}

	@Converter(autoApply = true)
	static class TestComponentListConverter extends JsonConverter<List<TestComponent>> {
	}

}
