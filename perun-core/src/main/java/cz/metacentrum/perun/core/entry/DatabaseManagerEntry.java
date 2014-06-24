package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.DatabaseManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database manager can work with database version and upgraded state of perun DB.
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public class DatabaseManagerEntry implements DatabaseManager {
	final static Logger log = LoggerFactory.getLogger(FacilitiesManagerEntry.class);

	private DatabaseManagerBl databaseManagerBl;
	private PerunBl perunBl;
	
	public DatabaseManagerEntry() {}

	public String getCurrentDatabaseVersion(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF)) throw new PrivilegeException(sess, "getCurrentDatabaseVersion");
		
		return getDatabaseManagerBl().getCurrentDatabaseVersion(sess);
	}
	
	public String getDatabaseDriverInformation(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF)) throw new PrivilegeException(sess, "getDatabaseDriverInformation");
		
		return getDatabaseManagerBl().getDatabaseDriverInformation(sess);
	}
	
	public String getDatabaseInformation(PerunSession sess) throws InternalErrorException, PrivilegeException {
		Utils.checkPerunSession(sess);
		
		// Authorization
		if (!AuthzResolver.isAuthorized(sess, Role.SELF)) throw new PrivilegeException(sess, "getDatabaseDriverInformation");
		
		return getDatabaseManagerBl().getDatabaseInformation(sess);
	}
	
	public void setDatabaseManagerBl(DatabaseManagerBl databaseManagerBl) {
		this.databaseManagerBl = databaseManagerBl;
	}
	
	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	public DatabaseManagerBl getDatabaseManagerBl() {
		return this.databaseManagerBl;
	}
	
	public PerunBl getPerunBl() {
		return this.perunBl;
	}
}
