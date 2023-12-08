package io.cornerstone.core.sequence.simple;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Locale;

import javax.sql.DataSource;

import io.cornerstone.core.jdbc.DatabaseProduct;

public class DatabaseSimpleSequenceDelegate extends AbstractDatabaseSimpleSequence {

	private AbstractDatabaseSimpleSequence seq = null;

	public DatabaseSimpleSequenceDelegate() {

	}

	public DatabaseSimpleSequenceDelegate(DataSource dataSource) {
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
			this.seq = new MySQLSimpleSequence();
		}
		else if (databaseProduct == DatabaseProduct.MARIADB) {
			this.seq = new MariaDBSimpleSequence();
		}
		else if (databaseProduct == DatabaseProduct.POSTGRESQL) {
			this.seq = new PostgreSQLSimpleSequence();
		}
		else if (databaseProduct == DatabaseProduct.ORACLE) {
			this.seq = new OracleSimpleSequence();
		}
		else if (databaseProduct == DatabaseProduct.DB2) {
			this.seq = new DB2SimpleSequence();
		}
		else if (databaseProduct == DatabaseProduct.INFORMIX) {
			this.seq = new InformixSimpleSequence();
		}
		else if (databaseProduct == DatabaseProduct.SQLSERVER) {
			this.seq = new SqlServerSimpleSequence();
		}
		else if (databaseProduct == DatabaseProduct.SYBASE) {
			this.seq = new SybaseSimpleSequence();
		}
		else if (databaseProduct == DatabaseProduct.H2) {
			this.seq = new H2SimpleSequence();
		}
		else if (databaseProduct == DatabaseProduct.HSQL) {
			this.seq = new HSQLSimpleSequence();
		}
		else if (databaseProduct == DatabaseProduct.DERBY) {
			this.seq = new DerbySimpleSequence();
		}
		else if (databaseProduct == DatabaseProduct.CUBRID) {
			this.seq = new CubridSimpleSequence();
		}
		else if (databaseProduct == DatabaseProduct.FIREBIRD) {
			this.seq = new FirebirdSimpleSequence();
		}
		else {
			throw new RuntimeException("not implemented for database " + databaseProduct);
		}
		this.seq.setDataSource(getDataSource());
		if (getCacheSize() > 1) {
			this.seq.setCacheSize(getCacheSize());
		}
		this.seq.setPaddingLength(getPaddingLength());
		this.seq.setTableName(getTableName());
		this.seq.setSequenceName(getSequenceName());
		this.seq.afterPropertiesSet();
	}

	@Override
	public void restart() {
		this.seq.restart();
	}

	@Override
	public long nextLongValue() {
		return this.seq.nextLongValue();
	}

}
