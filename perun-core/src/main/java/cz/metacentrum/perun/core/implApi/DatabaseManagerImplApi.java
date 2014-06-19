package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Database manager can work with database version and upgraded state of perun DB.
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public interface DatabaseManagerImplApi {
	/**
	 * Return current database version in string (ex. 3.0.1)
	 * 
	 * @param perunSession
	 * @return return current database version
	 * 
	 * @throws InternalErrorException
	 */
	String getCurrentDatabaseVersion(PerunSession perunSession) throws InternalErrorException;
}
