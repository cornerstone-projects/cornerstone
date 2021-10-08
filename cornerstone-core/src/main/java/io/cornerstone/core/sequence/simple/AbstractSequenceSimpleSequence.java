package io.cornerstone.core.sequence.simple;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

public abstract class AbstractSequenceSimpleSequence extends AbstractDatabaseSimpleSequence {

	protected abstract String getQuerySequenceStatement();

	protected String getCreateSequenceStatement() {
		StringBuilder sb = new StringBuilder("CREATE SEQUENCE ").append(getActualSequenceName());
		if (getCacheSize() > 1) {
			sb.append(" CACHE ").append(getCacheSize());
		}
		return sb.toString();
	}

	protected String getRestartSequenceStatement() {
		return "ALTER SEQUENCE " + getActualSequenceName() + " RESTART WITH 1";
	}

	@Override
	public void afterPropertiesSet() {
		try (Connection conn = getDataSource().getConnection(); Statement stmt = conn.createStatement()) {
			conn.setAutoCommit(true);
			if (!isSequenceExists(conn, getActualSequenceName())) {
				stmt.execute(getCreateSequenceStatement());
			}
		}
		catch (SQLException ex) {
			this.logger.warn(ex.getMessage());
		}
	}

	protected boolean isSequenceExists(Connection conn, String sequenceName) throws SQLException {
		DatabaseMetaData dbmd = conn.getMetaData();
		String catalog = conn.getCatalog();
		String schema = null;
		try {
			schema = conn.getSchema();
		}
		catch (Throwable th) {
		}
		for (String sequence : new LinkedHashSet<>(Arrays.asList(sequenceName.toUpperCase(Locale.ROOT), sequenceName,
				sequenceName.toLowerCase(Locale.ROOT)))) {
			try (ResultSet rs = dbmd.getTables(catalog, schema, sequence, new String[] { "SEQUENCE" })) {
				if (rs.next()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public long nextLongValue() throws DataAccessException {
		try (Connection con = getDataSource().getConnection();
				Statement stmt = con.createStatement();
				ResultSet rs = stmt.executeQuery(getQuerySequenceStatement())) {
			rs.next();
			return rs.getLong(1);
		}
		catch (SQLException ex) {
			throw new DataAccessResourceFailureException("Could not obtain next value of sequence", ex);
		}
	}

	@Override
	public void restart() {
		try (Connection con = getDataSource().getConnection(); Statement stmt = con.createStatement()) {
			con.setAutoCommit(true);
			restartSequence(con, stmt);
		}
		catch (SQLException ex) {
			throw new DataAccessResourceFailureException(ex.getMessage(), ex);
		}
	}

	protected void restartSequence(Connection con, Statement stmt) throws SQLException {
		stmt.execute(getRestartSequenceStatement());
	}

}
