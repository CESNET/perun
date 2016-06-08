package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

/**
 * BasicDataSource used instead of BasicDataSource in Perun to override getConnection.
 * 
 * @author Jiri Mauritz <jirmauritz at gmail dot com>
 */
public class PerunBasicDataSource extends BasicDataSource {

	private Auditer auditer;
	
	@Override
	public Connection getConnection() throws SQLException {
		
		Connection con = super.getConnection();
		//Set readOnly when working with any connection
		if(BeansUtils.isPerunReadOnly()) {
			con.setReadOnly(true);
		} else {
			con.setReadOnly(false);
		}

		return new PerunConnection(con, auditer);
	}

	public Auditer getAuditer() {
		return auditer;
	}

	public void setAuditer(Auditer auditer) {
		this.auditer = auditer;
	}
}
