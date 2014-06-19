package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.implApi.DatabaseManagerImplApi;
import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Database manager can work with database version and upgraded state of perun DB.
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public class DatabaseManagerImpl implements DatabaseManagerImplApi {
	final static Logger log = LoggerFactory.getLogger(DatabaseManagerImpl.class);
	private static JdbcTemplate jdbc;
	
	private static final String VERSION_PROPETY = "DATABASE VERSION";
	
	public DatabaseManagerImpl(DataSource perunPool) {
		jdbc = new JdbcTemplate(perunPool);
	}
	
	public String getCurrentDatabaseVersion(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.queryForObject("select value from configurations where property=?", String.class, VERSION_PROPETY);
		} catch(EmptyResultDataAccessException ex) {
			throw new ConsistencyErrorException(ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}
	
	public String getDatabaseDriverInformation(PerunSession sess) throws InternalErrorException {
		try {
			Connection con = ((BasicDataSource) jdbc.getDataSource()).getConnection();
			String driverVersion = con.getMetaData().getDriverVersion();
			String driverName = con.getMetaData().getDriverName();
			con.close();
			return driverName + "-" + driverVersion;
		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}
	}
	
	public String getDatabaseInformation(PerunSession sess) throws InternalErrorException {
		try {
			Connection con = ((BasicDataSource) jdbc.getDataSource()).getConnection();
			String dbName = con.getMetaData().getDatabaseProductName();
			String dbVersion = con.getMetaData().getDatabaseProductVersion();
			con.close();
			return dbName + "-" + dbVersion;
		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}
	}
}
