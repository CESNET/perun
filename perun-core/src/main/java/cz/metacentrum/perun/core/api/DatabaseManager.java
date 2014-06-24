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
	 * @param perunSession
	 * @return return current database version
	 * 
	 * @throws InternalErrorException
	 * @throws PrivilegeException 
	 */
	String getCurrentDatabaseVersion(PerunSession perunSession) throws InternalErrorException, PrivilegeException;
	
	/**
	 * Get DB driver information from datasource (name-version)
	 * 
	 * @param sess
	 * @return string information about database driver
	 * 
	 * @throws InternalErrorException
	 * @throws PrivilegeException 
	 */
	String getDatabaseDriverInformation(PerunSession sess) throws InternalErrorException, PrivilegeException;
	
	/**
	 * Get DB information from datasource (name-version)
	 * 
	 * @param sess
	 * @return string information about database
	 * 
	 * @throws InternalErrorException
	 * @throws PrivilegeException 
	 */
	String getDatabaseInformation(PerunSession sess) throws InternalErrorException, PrivilegeException;
}
