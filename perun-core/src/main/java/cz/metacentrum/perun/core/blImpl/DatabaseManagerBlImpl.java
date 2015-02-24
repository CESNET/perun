package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.DBVersion;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.DatabaseManagerBl;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.core.implApi.DatabaseManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Database manager can work with database version and upgraded state of perun DB.
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public class DatabaseManagerBlImpl implements DatabaseManagerBl {
	final static Logger log = LoggerFactory.getLogger(DatabaseManagerBlImpl.class);

	public static final String ORACLE_CHANGELOG = "oracleChangelog.txt";
	public static final String POSTGRES_CHANGELOG = "postgresChangelog.txt";
	public static final String HSQLDB_CHANGELOG = "hsqldbChangelog.txt";
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

	public void updateDatabaseVersion(List<DBVersion> dbVersions) throws InternalErrorException {
		this.databaseManagerImpl.updateDatabaseVersion(dbVersions);
	}

	public List<DBVersion> getChangelogVersions(String currentDBVersion, String fileName) throws InternalErrorException {
		return this.databaseManagerImpl.getChangelogVersions(currentDBVersion, fileName);
	}

	protected void initialize() throws InternalErrorException {
		log.debug("Initialize manager starts!");

		String fileName = POSTGRES_CHANGELOG;

		if (Compatibility.isOracle()) {
			fileName = ORACLE_CHANGELOG;
		} else if (Compatibility.isHSQLDB()) {
			fileName = HSQLDB_CHANGELOG;
		}

		String currentDBVersion = getCurrentDatabaseVersion();
		List<DBVersion> dbVersions = null;

		//trying to parse db versions from changelog
		try {
			dbVersions = getChangelogVersions(currentDBVersion, fileName);
		} catch (Exception e) {
			//Call exception which kills the initialization process in spring
			throw new InternalErrorException("Error parsing DB changelog file " + fileName, e);
		}

		String codeDBVersion = this.databaseManagerImpl.getCodeDatabaseVersion(dbVersions, currentDBVersion);

		if(codeDBVersion.equals(currentDBVersion)) {
			log.debug("DB version is up to date - CurrentVersion: " + currentDBVersion);
		} else {
			log.debug("DB version is not up to date! Updating database.");
			try {
				updateDatabaseVersion(dbVersions);
				log.debug("DB version updated successfully. Current version: " + codeDBVersion);
			} catch(Exception e){
				//Call exception which kills the initialization process in spring
				throw new InternalErrorException("DB version is NOT up to date, database update was unsuccessful! Look to the logs for more info.");
			}
		}
		log.debug("Initialize manager ends!");
	}
	
	public DatabaseManagerImplApi getDatabaseManagerImpl() {
		return this.databaseManagerImpl;
	}
}
