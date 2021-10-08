package io.cornerstone.core.jdbc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseProductTest {

	@Test
	void testParse() {
		assertThat(DatabaseProduct.parse("MySQL")).isEqualTo(DatabaseProduct.MYSQL);
		assertThat(DatabaseProduct.parse("jdbc:mysql://localhost:3306/test")).isEqualTo(DatabaseProduct.MYSQL);
		assertThat(DatabaseProduct.parse("MariaDB")).isEqualTo(DatabaseProduct.MARIADB);
		assertThat(DatabaseProduct.parse("jdbc:mariadb://localhost:3306/test")).isEqualTo(DatabaseProduct.MARIADB);
		assertThat(DatabaseProduct.parse("PostgreSQL")).isEqualTo(DatabaseProduct.POSTGRESQL);
		assertThat(DatabaseProduct.parse("jdbc:postgresql://localhost:5432/test"))
				.isEqualTo(DatabaseProduct.POSTGRESQL);
		assertThat(DatabaseProduct.parse("Oracle")).isEqualTo(DatabaseProduct.ORACLE);
		assertThat(DatabaseProduct.parse("jdbc:oracle:thin:@localhost:1521:XE")).isEqualTo(DatabaseProduct.ORACLE);
		assertThat(DatabaseProduct.parse("DB2")).isEqualTo(DatabaseProduct.DB2);
		assertThat(DatabaseProduct.parse("jdbc:db2://localhost:50000/test")).isEqualTo(DatabaseProduct.DB2);
		assertThat(DatabaseProduct.parse("Microsoft SQL Server")).isEqualTo(DatabaseProduct.SQLSERVER);
		assertThat(DatabaseProduct.parse("jdbc:sqlserver://localhost:1433;Database=test"))
				.isEqualTo(DatabaseProduct.SQLSERVER);
	}

}
