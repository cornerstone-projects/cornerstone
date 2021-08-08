package io.cornerstone.core.hibernate.integrated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

import io.cornerstone.core.hibernate.convert.EnumArrayConverter;
import io.cornerstone.core.hibernate.convert.EnumListConverter;
import io.cornerstone.core.hibernate.convert.EnumSetConverter;
import io.cornerstone.core.hibernate.convert.JsonConverter;
import io.cornerstone.core.hibernate.type.JsonType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@TypeDefs({ @TypeDef(name = "json", typeClass = JsonType.class) })
@Entity
@Getter
@Setter
@EqualsAndHashCode
public class TestEntity implements Persistable<Long> {

	@Id
	@GeneratedValue(generator = "snowflake")
	@GenericGenerator(name = "snowflake", strategy = "io.cornerstone.core.hibernate.id.SnowflakeIdentifierGenerator")
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

	@Type(type = "json")
	private List<AnotherComponent> anotherComponentList;

	@Override
	public boolean isNew() {
		return null == getId();
	}

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

	public static enum TestEnum {
		A, B, C
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TestComponent {

		private String string;

		private Integer integer;

		private BigDecimal bigDecimal;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AnotherComponent {

		private String string;

		private Integer integer;

		private BigDecimal bigDecimal;
	}


}