package io.cornerstone.core.sequence.simple;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CubridSimpleSequence extends AbstractSequenceSimpleSequence {

	@Override
	protected boolean isSequenceExists(Connection conn, String sequenceName) throws SQLException {
		String sql = "SELECT NAME FROM DB_SERIAL";
		try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				if (sequenceName.equalsIgnoreCase(rs.getString("NAME")))
					return true;
			}
		}
		return false;
	}

	@Override
	protected String getCreateSequenceStatement() {
		StringBuilder sb = new StringBuilder("CREATE SERIAL ").append(getActualSequenceName());
		if (getCacheSize() > 1)
			sb.append(" CACHE ").append(getCacheSize());
		return sb.toString();
	}

	@Override
	protected String getQuerySequenceStatement() {
		return "SELECT " + getActualSequenceName() + ".NEXT_VALUE";
	}

	@Override
	protected void restartSequence(Connection con, Statement stmt) throws SQLException {
		stmt.execute("ALTER SERIAL " + getActualSequenceName() + " START WITH 1");
	}

}
