package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Database manager can work with database version and upgraded state of perun DB.
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public interface DatabaseManagerBl {
	/**
	 * Return current database version in string (ex. 3.0.1)
	 * 
	 * @return return current database version
	 * 
	 * @throws InternalErrorException
	 */
	String getCurrentDatabaseVersion() throws InternalErrorException;
	
	/**
	 * Get DB driver information from datasource (name-version)
	 * 
	 * @return string information about database driver
	 * 
	 * @throws InternalErrorException 
	 */
	String getDatabaseDriverInformation() throws InternalErrorException;
	
	/**
	 * Get DB information from datasource (name-version)
	 * 
	 * @return string information about database
	 * 
	 * @throws InternalErrorException 
	 */
	String getDatabaseInformation() throws InternalErrorException;

	/**
	 * Test if DB version is the last possible.
	 * Use version of DB in DatabaseManager and version of DB in DB itself.
	 *
	 * @return return true if DB is upToDate, false if not
	 *
	 * @throws InternalErrorException
	 */
	boolean isDBVersionUpToDate() throws InternalErrorException;
}
