package io.cornerstone.core.sequence.cyclic;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

import io.cornerstone.core.util.MaxAttemptsExceededException;
import lombok.Value;

public abstract class AbstractSequenceCyclicSequence extends AbstractDatabaseCyclicSequence {

	private String querySequenceStatement;

	private String queryTimestampForUpdateStatement;

	private String updateTimestampStatement;

	protected String getTimestampColumnType() {
		return "TIMESTAMP";
	}

	protected String getNameColumnType() {
		return "VARCHAR(50)";
	}

	protected String getCurrentTimestamp() {
		return "CURRENT_TIMESTAMP";
	}

	protected String getCreateTableStatement() {
		return "CREATE TABLE " + getTableName() + " (NAME " + getNameColumnType()
				+ " NOT NULL PRIMARY KEY, LAST_UPDATED " + getTimestampColumnType() + " NOT NULL)";
	}

	protected String getInsertStatement() {
		return "INSERT INTO " + getTableName() + " VALUES(" + "'" + getSequenceName() + "'," + getCurrentTimestamp()
				+ ")";
	}

	protected abstract String getQuerySequenceStatement();

	protected String getCreateSequenceStatement() {
		StringBuilder sb = new StringBuilder("CREATE SEQUENCE ").append(getActualSequenceName());
		if (getCacheSize() > 1)
			sb.append(" CACHE ").append(getCacheSize());
		return sb.toString();
	}

	protected String getRestartSequenceStatement() {
		return "ALTER SEQUENCE " + getActualSequenceName() + " RESTART WITH 1";
	}

	protected String getQueryTimestampForUpdateStatement() {
		return "SELECT " + getCurrentTimestamp() + ",LAST_UPDATED" + " FROM " + getTableName() + " WHERE NAME='"
				+ getSequenceName() + "' FOR UPDATE";
	}

	protected String getUpdateTimestampStatement() {
		return "UPDATE " + getTableName() + " SET LAST_UPDATED = ? WHERE NAME='" + getSequenceName()
				+ "' AND LAST_UPDATED < ?";
	}

	@Override
	public void afterPropertiesSet() {
		this.querySequenceStatement = getQuerySequenceStatement();
		this.queryTimestampForUpdateStatement = getQueryTimestampForUpdateStatement();
		this.updateTimestampStatement = getUpdateTimestampStatement();
		try {
			createOrUpgradeTable();
		} catch (SQLException ex) {
			this.logger.error(ex.getMessage(), ex);
		}
	}

	protected void createOrUpgradeTable() throws SQLException {
		try (Connection conn = getDataSource().getConnection(); Statement stmt = conn.createStatement()) {
			String tableName = getTableName();
			boolean tableExists = false;
			conn.setAutoCommit(true);
			DatabaseMetaData dbmd = conn.getMetaData();
			String catalog = conn.getCatalog();
			String schema = null;
			try {
				schema = conn.getSchema();
			} catch (Throwable t) {
			}
			for (String table : new LinkedHashSet<>(
					Arrays.asList(tableName.toUpperCase(Locale.ROOT), tableName, tableName.toLowerCase(Locale.ROOT)))) {
				try (ResultSet rs = dbmd.getTables(catalog, schema, table, new String[] { "TABLE" })) {
					if (rs.next()) {
						tableExists = true;
						break;
					}
				}
			}
			if (tableExists) {
				// upgrade legacy
				Map<String, Object> map = null;
				try (ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
					ResultSetMetaData rsmd = rs.getMetaData();
					boolean legacy = true;
					for (int i = 0; i < rsmd.getColumnCount(); i++)
						if ("LAST_UPDATED".equalsIgnoreCase(rsmd.getColumnName(i + 1))) {
							legacy = false;
							break;
						}
					if (legacy) {
						map = new LinkedHashMap<>();
						rs.next();
						for (int i = 0; i < rsmd.getColumnCount(); i++)
							map.put(rsmd.getColumnName(i + 1), rs.getObject(i + 1));
					}
				}
				if (map != null) {
					stmt.execute("DROP TABLE " + tableName);
					stmt.execute(getCreateTableStatement());
					try (PreparedStatement ps = conn.prepareStatement("INSERT INTO " + tableName + " VALUES(?,?)")) {
						for (Map.Entry<String, Object> entry : map.entrySet()) {
							if (entry.getKey().toUpperCase(Locale.ROOT).endsWith("_TIMESTAMP")) {
								String sequenceName = entry.getKey();
								sequenceName = sequenceName.substring(0, sequenceName.lastIndexOf('_'));
								ps.setString(1, sequenceName);
								ps.setTimestamp(2, ((Timestamp) entry.getValue()));
								ps.addBatch();
							}
						}
						ps.executeBatch();
					}
				}
			}
			String sequenceName = getSequenceName();
			if (tableExists) {
				boolean rowExists = false;
				try (ResultSet rs = stmt
						.executeQuery("SELECT NAME FROM " + tableName + " WHERE NAME='" + sequenceName + "'")) {
					rowExists = rs.next();
				}
				if (!rowExists) {
					stmt.execute(getInsertStatement());
					stmt.execute(getCreateSequenceStatement());
				}
			} else {
				stmt.execute(getCreateTableStatement());
				stmt.execute(getInsertStatement());
				stmt.execute(getCreateSequenceStatement());
			}
		}
	}

	@Override
	public String nextStringValue() throws DataAccessException {
		try (Connection con = getDataSource().getConnection(); Statement stmt = con.createStatement()) {
			con.setAutoCommit(true);
			CycleType ct = getCycleType();
			int maxAttempts = 3;
			int remainingAttempts = maxAttempts;
			do {
				Result result = queryTimestampWithSequence(con, stmt);
				LocalDateTime now = result.current;
				if (sameCycle(result)) {
					if (updateLastUpdated(con, now, ct.getCycleStart(ct.skipCycles(now, 1))))
						return getStringValue(now, getPaddingLength(), result.nextId);
				} else {
					con.setAutoCommit(false);
					try {
						result = queryTimestampForUpdate(con, stmt);
						if (!sameCycle(result) && updateLastUpdated(con, now, ct.getCycleStart(now))) {
							restartSequence(con, stmt);
							result = queryTimestampWithSequence(con, stmt);
							return getStringValue(result.current, getPaddingLength(), result.nextId);
						}
						con.commit();
					} catch (Exception ex) {
						con.rollback();
						throw new DataAccessResourceFailureException(ex.getMessage(), ex);
					} finally {
						con.setAutoCommit(true);
					}
				}
				try {
					Thread.sleep(((1 + maxAttempts) - remainingAttempts) * 50);
				} catch (InterruptedException ex) {
					this.logger.warn(ex.getMessage(), ex);
				}
			} while (--remainingAttempts > 0);
			throw new MaxAttemptsExceededException(maxAttempts);
		} catch (SQLException ex) {
			throw new DataAccessResourceFailureException("Could not obtain next value of sequence", ex);
		}
	}

	protected void restartSequence(Connection con, Statement stmt) throws SQLException {
		stmt.execute(getRestartSequenceStatement());
	}

	private boolean updateLastUpdated(Connection con, LocalDateTime lastUpdated, LocalDateTime limit)
			throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(this.updateTimestampStatement)) {
			ps.setTimestamp(1, Timestamp.valueOf(lastUpdated));
			ps.setTimestamp(2, Timestamp.valueOf(limit));
			return ps.executeUpdate() == 1;
		}
	}

	private boolean sameCycle(Result result) {
		return getCycleType().isSameCycle(result.last, result.current);
	}

	private Result queryTimestampWithSequence(Connection con, Statement stmt) throws SQLException {
		try (ResultSet rs = stmt.executeQuery(this.querySequenceStatement)) {
			rs.next();
			int nextId = rs.getInt(1);
			Timestamp currentTimestamp = rs.getTimestamp(2);
			Timestamp lastTimestamp = rs.getTimestamp(3);
			// keep monotonic incrementing
			if (lastTimestamp.after(currentTimestamp))
				currentTimestamp = lastTimestamp;
			return new Result(nextId, currentTimestamp.toLocalDateTime(), lastTimestamp.toLocalDateTime());
		}
	}

	private Result queryTimestampForUpdate(Connection con, Statement stmt) throws SQLException {
		try (ResultSet rs = stmt.executeQuery(this.queryTimestampForUpdateStatement)) {
			rs.next();
			Timestamp currentTimestamp = rs.getTimestamp(1);
			Timestamp lastTimestamp = rs.getTimestamp(2);
			// keep monotonic incrementing
			if (lastTimestamp.after(currentTimestamp))
				currentTimestamp = lastTimestamp;
			return new Result(0, currentTimestamp.toLocalDateTime(), lastTimestamp.toLocalDateTime());
		}
	}

	@Value
	private static class Result {
		int nextId;
		LocalDateTime current;
		LocalDateTime last;
	}
}
