package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.DBVersion;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.core.impl.Utils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;

import java.util.List;

/**
 * Database manager can work with database version and upgraded state of perun DB.
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public interface DatabaseManagerImplApi {
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
	 * Returns current code version from dbVersions list (ex. 3.0.1) or currentDBVersion if list is empty (because in that case currentDBVersion = current code version)
	 *
	 * @param dbVersions list of DBVersion objects
	 * @param currentDBVersion current DB version
	 *
	 * @return current code version
	 */
	String getCodeDatabaseVersion(List<DBVersion> dbVersions, String currentDBVersion);

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
	 * @throws InternalErrorException if 1.there is error reading file, 2.currentDBVersion was not found 3.db version does not match pattern 4.db versions are not ordered as they should be
	 */
	List<DBVersion> getChangelogVersions(String currentDBVersion, String fileName) throws InternalErrorException;

	/**
	 *	Get time in ns "nanoseconds" of calling 1 simple update query to DB.
	 *  This query will update property for this purpose in configurations table.
	 *
	 * @return time of processing query in nanoseconds
	 */
	long getTimeOfQueryPerformance();

	/**
	 * Create new property in configurations. Initial value will be "N/A".
	 *
	 * @param property name of property to be created
	 */
	void createProperty(String property);

	/**
	 * Return true if property already exists, false if not.
	 *
	 * @param property name of property to check existence
	 *
	 * @return true if property exists, false if not
	 */
	boolean propertyExists(String property);

	/**
	 * Return JDBC template for performing custom simple SQLs where jdbc is not normally available
	 *
	 * @return Peruns JDBC template
	 */
	JdbcPerunTemplate getJdbcPerunTemplate();

}
