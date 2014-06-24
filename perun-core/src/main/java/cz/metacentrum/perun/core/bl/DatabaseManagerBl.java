package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.PerunSession;
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
	 * @param perunSession
	 * @return return current database version
	 * 
	 * @throws InternalErrorException
	 */
	String getCurrentDatabaseVersion(PerunSession perunSession) throws InternalErrorException;
	
	/**
	 * Get DB driver information from datasource (name-version)
	 * 
	 * @param sess
	 * @return string information about database driver
	 * 
	 * @throws InternalErrorException 
	 */
	String getDatabaseDriverInformation(PerunSession sess) throws InternalErrorException;
	
	/**
	 * Get DB information from datasource (name-version)
	 * 
	 * @param sess
	 * @return string information about database
	 * 
	 * @throws InternalErrorException 
	 */
	String getDatabaseInformation(PerunSession sess) throws InternalErrorException;
}
