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
		DatabaseProduct databaseProduct = null;
		try (Connection con = getDataSource().getConnection()) {
			DatabaseMetaData dbmd = con.getMetaData();
			databaseProduct = DatabaseProduct.parse(dbmd.getDatabaseProductName().toLowerCase(Locale.ROOT));
		}
		if (databaseProduct == DatabaseProduct.MYSQL)
			seq = new MySQLCyclicSequence();
		else if (databaseProduct == DatabaseProduct.MARIADB)
			seq = new MariaDBCyclicSequence();
		else if (databaseProduct == DatabaseProduct.POSTGRESQL)
			seq = new PostgreSQLCyclicSequence();
		else if (databaseProduct == DatabaseProduct.ORACLE)
			seq = new OracleCyclicSequence();
		else if (databaseProduct == DatabaseProduct.DB2)
			seq = new DB2CyclicSequence();
		else if (databaseProduct == DatabaseProduct.INFORMIX)
			seq = new InformixCyclicSequence();
		else if (databaseProduct == DatabaseProduct.SQLSERVER)
			seq = new SqlServerCyclicSequence();
		else if (databaseProduct == DatabaseProduct.SYBASE)
			seq = new SybaseCyclicSequence();
		else if (databaseProduct == DatabaseProduct.H2)
			seq = new H2CyclicSequence();
		else if (databaseProduct == DatabaseProduct.HSQL)
			seq = new HSQLCyclicSequence();
		else if (databaseProduct == DatabaseProduct.DERBY)
			seq = new DerbyCyclicSequence();
		else if (databaseProduct == DatabaseProduct.CUBRID)
			seq = new CubridCyclicSequence();
		else if (databaseProduct == DatabaseProduct.FIREBIRD)
			seq = new FirebirdCyclicSequence();
		else
			throw new RuntimeException("not implemented for database " + databaseProduct);
		seq.setDataSource(getDataSource());
		if (getCacheSize() > 1)
			seq.setCacheSize(getCacheSize());
		seq.setCycleType(getCycleType());
		seq.setPaddingLength(getPaddingLength());
		seq.setTableName(getTableName());
		seq.setSequenceName(getSequenceName());
		seq.afterPropertiesSet();
	}

	@Override
	public String nextStringValue() {
		return seq.nextStringValue();
	}

}
