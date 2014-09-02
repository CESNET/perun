package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import cz.metacentrum.perun.core.entry.DatabaseManagerEntry;
import cz.metacentrum.perun.core.implApi.DatabaseManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database manager can work with database version and upgraded state of perun DB.
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public class DatabaseManagerBlImpl implements DatabaseManagerBl {
	final static Logger log = LoggerFactory.getLogger(DatabaseManagerBlImpl.class);

	private final DatabaseManagerImplApi databaseManagerImpl;
	
	public DatabaseManagerBlImpl(DatabaseManagerImplApi databaseManagerImpl) {
		this.databaseManagerImpl = databaseManagerImpl;
	}
	
	public String getCurrentDatabaseVersion() throws InternalErrorException {
		return getDatabaseManagerImpl().getCurrentDatabaseVersion();
	}
	
	public String getDatabaseDriverInformation() throws InternalErrorException {
		return getDatabaseManagerImpl().getDatabaseDriverInformation();
	}
	
	public String getDatabaseInformation() throws InternalErrorException {
		return getDatabaseManagerImpl().getDatabaseInformation();
	}

	public boolean isDBVersionUpToDate() throws InternalErrorException {
		if (DatabaseManagerEntry.ACTUAL_DB_VERSION.equals(getCurrentDatabaseVersion())) {
			return true;
		} else {
			return false;
		}
	}

	protected void initialize() throws InternalErrorException {
		log.debug("Initialize manager starts!");
		if(isDBVersionUpToDate()) {
			log.debug("DB version is Up to date - CurrentVersion: " + getCurrentDatabaseVersion());
		} else {
			log.error("DB version is NOT up to date! CurrentCodeVersion: " + DatabaseManagerEntry.ACTUAL_DB_VERSION + ", CurrentDBVersion: " + getCurrentDatabaseVersion());
			//Call exception which kill the initialization process in spring
			throw new InternalErrorException("DB version is NOT up to date! Look to the logs for more info.");
		}
		log.debug("Initialize manager ends!");
	}
	
	public DatabaseManagerImplApi getDatabaseManagerImpl() {
		return this.databaseManagerImpl;
	}
}
