package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;

/**
 * Database manager can work with database version and upgraded state of perun DB.
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public interface DatabaseManager {
	
	/**
	 * Return current database version in string (ex. 3.0.1)
	 * 
	 * @return return current database version
	 */
	String getCurrentDatabaseVersion(PerunSession perunSession) throws InternalErrorException, PrivilegeException;
	
	/**
	 * Get DB driver information from datasource (name-version)
	 * 
	 * @return string information about database driver
	 */
	String getDatabaseDriverInformation(PerunSession sess) throws InternalErrorException, PrivilegeException;
	
	/**
	 * Get DB information from datasource (name-version)
	 * 
	 * @return string information about database
	 * 
	 */
	String getDatabaseInformation(PerunSession sess) throws InternalErrorException, PrivilegeException;
}
