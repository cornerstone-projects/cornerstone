package io.cornerstone.core.persistence.integrated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.cornerstone.core.persistence.convert.EnumArrayConverter;
import io.cornerstone.core.persistence.convert.EnumListConverter;
import io.cornerstone.core.persistence.convert.EnumSetConverter;
import io.cornerstone.core.persistence.convert.JsonConverter;
import io.cornerstone.core.persistence.id.SnowflakeIdentifierGenerator;
import io.cornerstone.core.persistence.type.JsonType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

@Entity
@Getter
@Setter
@EqualsAndHashCode
class TestEntity implements Persistable<Long> {

	@Id
	@GeneratedValue(generator = "snowflake")
	@GenericGenerator(name = "snowflake", type = SnowflakeIdentifierGenerator.class)
	private @Nullable Long id;

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

	@Type(JsonType.class)
	private List<AnotherComponent> anotherComponentList;

	@Override
	public boolean isNew() {
		return null == getId();
	}

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

	enum TestEnum {

		A, B, C

	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class TestComponent {

		private String string;

		private Integer integer;

		private BigDecimal bigDecimal;

	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class AnotherComponent {

		private String string;

		private Integer integer;

		private BigDecimal bigDecimal;

	}

}
