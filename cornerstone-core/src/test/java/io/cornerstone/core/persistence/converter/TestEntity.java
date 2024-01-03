package io.cornerstone.core.persistence.converter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.cornerstone.core.persistence.convert.EnumArrayConverter;
import io.cornerstone.core.persistence.convert.EnumListConverter;
import io.cornerstone.core.persistence.convert.EnumSetConverter;
import io.cornerstone.core.persistence.convert.JsonConverter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import org.springframework.data.jpa.domain.AbstractPersistable;

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
