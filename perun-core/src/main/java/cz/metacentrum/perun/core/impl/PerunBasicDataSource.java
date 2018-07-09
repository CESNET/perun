package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * BasicDataSource used instead of BasicDataSource in Perun to override getConnection.
 * 
 * @author Jiri Mauritz <jirmauritz at gmail dot com>
 */
public class PerunBasicDataSource extends BasicDataSource {

	private Auditer auditer;
	private CacheManager cacheManager;

	@Override
	public Connection getConnection() throws SQLException {
		
		Connection con = super.getConnection();
		//Set readOnly when working with any connection
		if(BeansUtils.isPerunReadOnly()) {
			con.setReadOnly(true);
		} else {
			con.setReadOnly(false);
		}

		return new PerunConnection(con, auditer, cacheManager);
	}

	public Auditer getAuditer() {
		return auditer;
	}

	public void setAuditer(Auditer auditer) {
		this.auditer = auditer;
	}

	public CacheManager getCacheManager() {
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}
