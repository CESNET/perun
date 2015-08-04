package cz.metacentrum.perun.core.impl;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

/**
 * BasicDataSource used instead of BasicDataSource in Perun to override getConnection.
 * 
 * @author Jiri Mauritz <jirmauritz at gmail dot com>
 */
public class PerunBasicDataSource extends BasicDataSource {

	@Override
	public Connection getConnection() throws SQLException {
		return new PerunConnection(super.getConnection());
	}
	
}
