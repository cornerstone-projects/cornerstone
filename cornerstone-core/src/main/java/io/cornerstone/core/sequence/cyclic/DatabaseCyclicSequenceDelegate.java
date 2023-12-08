package io.cornerstone.core.sequence.cyclic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Locale;

import javax.sql.DataSource;

import io.cornerstone.core.jdbc.DatabaseProduct;

public class DatabaseCyclicSequenceDelegate extends AbstractDatabaseCyclicSequence {

	private AbstractDatabaseCyclicSequence seq = null;

	public DatabaseCyclicSequenceDelegate() {

	}

	public DatabaseCyclicSequenceDelegate(DataSource dataSource) {
		setDataSource(dataSource);
	}

	@Override
	public void afterPropertiesSet() throws java.lang.Exception {
		DatabaseProduct databaseProduct;
		try (Connection con = getDataSource().getConnection()) {
			DatabaseMetaData dbmd = con.getMetaData();
			databaseProduct = DatabaseProduct.parse(dbmd.getDatabaseProductName().toLowerCase(Locale.ROOT));
		}
		if (databaseProduct == DatabaseProduct.MYSQL) {
			this.seq = new MySQLCyclicSequence();
		}
		else if (databaseProduct == DatabaseProduct.MARIADB) {
			this.seq = new MariaDBCyclicSequence();
		}
		else if (databaseProduct == DatabaseProduct.POSTGRESQL) {
			this.seq = new PostgreSQLCyclicSequence();
		}
		else if (databaseProduct == DatabaseProduct.ORACLE) {
			this.seq = new OracleCyclicSequence();
		}
		else if (databaseProduct == DatabaseProduct.DB2) {
			this.seq = new DB2CyclicSequence();
		}
		else if (databaseProduct == DatabaseProduct.INFORMIX) {
			this.seq = new InformixCyclicSequence();
		}
		else if (databaseProduct == DatabaseProduct.SQLSERVER) {
			this.seq = new SqlServerCyclicSequence();
		}
		else if (databaseProduct == DatabaseProduct.SYBASE) {
			this.seq = new SybaseCyclicSequence();
		}
		else if (databaseProduct == DatabaseProduct.H2) {
			this.seq = new H2CyclicSequence();
		}
		else if (databaseProduct == DatabaseProduct.HSQL) {
			this.seq = new HSQLCyclicSequence();
		}
		else if (databaseProduct == DatabaseProduct.DERBY) {
			this.seq = new DerbyCyclicSequence();
		}
		else if (databaseProduct == DatabaseProduct.CUBRID) {
			this.seq = new CubridCyclicSequence();
		}
		else if (databaseProduct == DatabaseProduct.FIREBIRD) {
			this.seq = new FirebirdCyclicSequence();
		}
		else {
			throw new RuntimeException("not implemented for database " + databaseProduct);
		}
		this.seq.setDataSource(getDataSource());
		if (getCacheSize() > 1) {
			this.seq.setCacheSize(getCacheSize());
		}
		this.seq.setCycleType(getCycleType());
		this.seq.setPaddingLength(getPaddingLength());
		this.seq.setTableName(getTableName());
		this.seq.setSequenceName(getSequenceName());
		this.seq.afterPropertiesSet();
	}

	@Override
	public String nextStringValue() {
		return this.seq.nextStringValue();
	}

}
