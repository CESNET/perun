package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
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
	private PerunBl perunBl;
	
	public DatabaseManagerBlImpl(DatabaseManagerImplApi databaseManagerImpl) {
		this.databaseManagerImpl = databaseManagerImpl;
	}
	
	public String getCurrentDatabaseVersion(PerunSession sess) throws InternalErrorException {
		return getDatabaseManagerImpl().getCurrentDatabaseVersion(sess);
	}
	
	public String getDatabaseDriverInformation(PerunSession sess) throws InternalErrorException {
		return getDatabaseManagerImpl().getDatabaseDriverInformation(sess);
	}
	
	public String getDatabaseInformation(PerunSession sess) throws InternalErrorException {
		return getDatabaseManagerImpl().getDatabaseInformation(sess);
	}
	
	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}
	
	public DatabaseManagerImplApi getDatabaseManagerImpl() {
		return this.databaseManagerImpl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}
}
