package io.cornerstone.core.hibernate.type;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestComponent {

	private String string;

	private Integer integer;

	private BigDecimal bigDecimal;
}
