package com.example.demo.core.hibernate.type;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DummyComponent {

	private String string;

	private Integer integer;

	private BigDecimal bigDecimal;
}
