package cz.metacentrum.perun.core.impl;

import com.zaxxer.hikari.HikariDataSource;
import cz.metacentrum.perun.core.api.BeansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * DataSource used instead of HikariDataSource in Perun to override getConnection.
 *
 * @author Jiri Mauritz <jirmauritz at gmail dot com>
 */
public class PerunBasicDataSource extends HikariDataSource {

	private static final Logger log = LoggerFactory.getLogger(PerunBasicDataSource.class);

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

	@Override
	public void setJdbcUrl(String jdbcUrl) {
		//for PostgreSQL, adds system property ApplicationName to URL, it is diplayed as application_name in
		// SELECT usename||'@'||datname AS who,application_name AS app,client_addr,state,query_start FROM pg_stat_activity ORDER BY app;
		String applicationName = System.getProperty("ApplicationName");
		if(applicationName!=null && jdbcUrl.contains("jdbc:postgresql")) {
			if(jdbcUrl.contains("?")) {
				jdbcUrl+= "&ApplicationName="+ URLEncoder.encode(applicationName);
			} else {
				jdbcUrl+= "?ApplicationName="+ URLEncoder.encode(applicationName);
			}
			log.info("changed jdbc.url to include ApplicationName: {}",jdbcUrl);
		}
		super.setJdbcUrl(jdbcUrl);
	}
}
