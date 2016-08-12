package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.DBVersion;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.springframework.jdbc.core.JdbcPerunTemplate;

import java.util.List;

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
	 * Method updates database to the current code version. It takes list of dbVersions and executes all the commands from them.
	 * Commands from the oldest (lowest) version are executed first.
	 *
	 * @param dbVersions list of dbVersion objects ordered by version descending, should not be empty
	 *
	 * @throws InternalErrorException if any of the commands fails to execute
	 */
	void updateDatabaseVersion(List<DBVersion> dbVersions) throws InternalErrorException;

	/**
	 * Parses all new database versions from DB changelog file and creates from them list of DBVersion objects.
	 * The list contains all versions from currentDBVersion (without currentDBVersion itself) to now (the version at the top of the changelog file)
	 *
	 * @param currentDBVersion current DB version
	 * @param fileName DB changelog file name, file should be in resources
	 *
	 * @return list of DBVersion objects ordered by version descending
	 *
	 * @throws InternalErrorException if 1.there is an error reading file, 2.currentDBVersion was not found 3.DBVersion does not match pattern 4.DB versions are not ordered as they should be
	 */
	List<DBVersion> getChangelogVersions(String currentDBVersion, String fileName) throws InternalErrorException;


	/**
	 * Return JDBC template for performing custom simple SQLs where jdbc is not normally available
	 *
	 * @return Peruns JDBC template
	 */
	public JdbcPerunTemplate getJdbcPerunTemplate();

}
