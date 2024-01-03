package io.cornerstone.core.persistence.type;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class TestComponent {

	private String string;

	private Integer integer;

	private BigDecimal bigDecimal;

}
