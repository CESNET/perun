package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.DatabaseManager;
import cz.metacentrum.perun.core.api.PerunSession;
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
	final static Logger log = LoggerFactory.getLogger(DatabaseManagerEntry.class);

	private DatabaseManagerBl databaseManagerBl;
	private PerunBl perunBl;

	public DatabaseManagerEntry() {}

	@Override
	public String getCurrentDatabaseVersion(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getCurrentDatabaseVersion_policy"))
			throw new PrivilegeException(sess, "getCurrentDatabaseVersion");

		return getDatabaseManagerBl().getCurrentDatabaseVersion();
	}

	@Override
	public String getDatabaseDriverInformation(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getDatabaseDriverInformation_policy"))
			throw new PrivilegeException(sess, "getDatabaseDriverInformation");

		return getDatabaseManagerBl().getDatabaseDriverInformation();
	}

	@Override
	public String getDatabaseInformation(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getDatabaseInformation_policy"))
			throw new PrivilegeException(sess, "getDatabaseInformation");

		return getDatabaseManagerBl().getDatabaseInformation();
	}

	@Override
	public long getTimeOfQueryPerformance(PerunSession sess) throws PrivilegeException {
		Utils.checkPerunSession(sess);

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getTimeOfQueryPerformance_policy"))
			throw new PrivilegeException(sess, "getTimeOfQueryPerformance");

		return getDatabaseManagerBl().getTimeOfQueryPerformance();
	}

	public void setDatabaseManagerBl(DatabaseManagerBl databaseManagerBl) {
		this.databaseManagerBl = databaseManagerBl;
	}

	public DatabaseManagerBl getDatabaseManagerBl() {
		return this.databaseManagerBl;
	}
}
