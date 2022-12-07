package io.cornerstone.core.sequence.cyclic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

import io.cornerstone.core.sequence.simple.MySQLSimpleSequence;
import io.cornerstone.core.util.MaxAttemptsExceededException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

public class MySQLCyclicSequence extends AbstractDatabaseCyclicSequence {

	private String setVariableSql;

	private String incrementSql;

	private String restartSql;

	private String selectLastInsertIdSql = "SELECT LAST_INSERT_ID(),@TIMESTAMP";

	@Override
	public void afterPropertiesSet() {
		this.setVariableSql = "SELECT @TIMESTAMP:=GREATEST(LAST_UPDATED,UNIX_TIMESTAMP()) FROM `" + getTableName()
				+ "` WHERE NAME='" + getSequenceName() + "'";
		this.incrementSql = "UPDATE `" + getTableName()
				+ "` SET VALUE=LAST_INSERT_ID(VALUE+1),LAST_UPDATED=@TIMESTAMP WHERE NAME='" + getSequenceName()
				+ "' AND DATE_FORMAT(FROM_UNIXTIME(LAST_UPDATED),'" + getDateFormat()
				+ "')=DATE_FORMAT(FROM_UNIXTIME(@TIMESTAMP),'" + getDateFormat() + "')";
		this.restartSql = "UPDATE `" + getTableName()
				+ "` SET VALUE=LAST_INSERT_ID(1),LAST_UPDATED=@TIMESTAMP WHERE NAME='" + getSequenceName()
				+ "' AND DATE_FORMAT(FROM_UNIXTIME(LAST_UPDATED),'" + getDateFormat()
				+ "')!=DATE_FORMAT(FROM_UNIXTIME(@TIMESTAMP),'" + getDateFormat() + "')";
		try {
			MySQLSimpleSequence.createTable(getDataSource(), getTableName(), getSequenceName());
		}
		catch (SQLException ex) {
			this.logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	public String nextStringValue() throws DataAccessException {
		try (Connection con = getDataSource().getConnection(); Statement stmt = con.createStatement()) {
			con.setAutoCommit(true);
			int maxAttempts = 3;
			int remainingAttempts = maxAttempts;
			do {
				stmt.execute(this.setVariableSql);
				int rows = stmt.executeUpdate(this.incrementSql);
				if (rows == 1) {
					return nextId(stmt);
				}
				stmt.execute(this.setVariableSql);
				rows = stmt.executeUpdate(this.restartSql);
				if (rows == 1) {
					return nextId(stmt);
				}
				try {
					Thread.sleep(((1 + maxAttempts) - remainingAttempts) * 50);
				}
				catch (InterruptedException ex) {
					this.logger.warn(ex.getMessage(), ex);
				}
			}
			while (--remainingAttempts > 0);
			throw new MaxAttemptsExceededException(maxAttempts);
		}
		catch (SQLException ex) {
			throw new DataAccessResourceFailureException("Could not obtain last_insert_id()", ex);
		}

	}

	private String nextId(Statement stmt) throws SQLException {
		try (ResultSet rs = stmt.executeQuery(this.selectLastInsertIdSql)) {
			if (!rs.next()) {
				throw new DataAccessResourceFailureException("LAST_INSERT_ID() failed after executing an update");
			}
			int next = rs.getInt(1);
			Long current = rs.getLong(2);
			if (current < 10000000000L) {
				current *= 1000;
			}
			LocalDateTime datetime = LocalDateTime.ofInstant(Instant.ofEpochMilli(current),
					TimeZone.getDefault().toZoneId());

			return getStringValue(datetime, getPaddingLength(), next);
		}
	}

	private String getDateFormat() {
		switch (getCycleType()) {
			case MINUTE:
				return "%Y%m%d%H%i";
			case HOUR:
				return "%Y%m%d%H";
			case DAY:
				return "%Y%m%d";
			case MONTH:
				return "%Y%m";
			case YEAR:
				return "%Y";
			default:
				throw new UnsupportedOperationException("Unknown cycle type");
		}
	}

}
