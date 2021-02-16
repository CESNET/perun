package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.DBVersion;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.Compatibility;
import org.springframework.jdbc.core.JdbcPerunTemplate;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Database manager can work with database version and upgraded state of perun DB.
 *
 * @author Michal Stava email:&lt;stavamichal@gmail.com&gt;
 */
public interface DatabaseManagerBl {

	String NAME_OF_ORACLE_ARRAY_METHOD = "createOracleArray";

	/**
	 * Return current database version in string (ex. 3.0.1)
	 *
	 * @return return current database version
	 *
	 * @throws InternalErrorException
	 */
	String getCurrentDatabaseVersion();

	/**
	 * Get DB driver information from datasource (name-version)
	 *
	 * @return string information about database driver
	 *
	 * @throws InternalErrorException
	 */
	String getDatabaseDriverInformation();

	/**
	 * Get DB information from datasource (name-version)
	 *
	 * @return string information about database
	 *
	 * @throws InternalErrorException
	 */
	String getDatabaseInformation();

	/**
	 * Method updates database to the current code version. It takes list of dbVersions and executes all the commands from them.
	 * Commands from the oldest (lowest) version are executed first.
	 *
	 * @param dbVersions list of dbVersion objects ordered by version descending, should not be empty
	 *
	 * @throws InternalErrorException if any of the commands fails to execute
	 */
	void updateDatabaseVersion(List<DBVersion> dbVersions);

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
	List<DBVersion> getChangelogVersions(String currentDBVersion, String fileName);

	/**
	 * Take list of perunBeans and generate an array of ids in sql database from it.
	 * Implementation can be different for every type of supported DB
	 *
	 * @param perunBeans list of PerunBeans to get Ids from
	 * @param preparedStatement database prepared statement to get working connection
	 * @return java sql array with pre-loaded list of ids
	 * @throws SQLException if any sql exception has been thrown
	 * @throws InternalErrorException if oracle method to work with an array can't be get or invoked
	 */
	static java.sql.Array prepareSQLArrayOfNumbers(List<? extends PerunBean> perunBeans, PreparedStatement preparedStatement) throws SQLException {
		List<Integer> listOfBeansIds = perunBeans.stream().map(PerunBean::getId).collect(Collectors.toList());
		return prepareSQLArrayOfNumbersFromIntegers(listOfBeansIds, preparedStatement);
	}

	/**
	 * Take list of integers and generate an array of integers in sql database from it.
	 * Implementation can be different for every type of supported DB
	 *
	 * @param integers list of integers
	 * @param preparedStatement database prepared statement to get working connection
	 * @return java sql array with pre-loaded list of integers
	 * @throws SQLException if any sql exception has been thrown
	 * @throws InternalErrorException if oracle method to work with an array can't be get or invoked
	 */
	static java.sql.Array prepareSQLArrayOfNumbersFromIntegers(List<Integer> integers, PreparedStatement preparedStatement) throws SQLException {
		Connection connection = preparedStatement.getConnection().unwrap(Connection.class);
		Integer[] arrayOfBeansIds = integers.toArray(new Integer[0]);
		return connection.createArrayOf(JDBCType.INTEGER.name(), arrayOfBeansIds);
	}
	/**
	 * Take list of String and generate an array in sql database from it.
	 * Implementation can be different for every type of supported DB
	 *
	 * @param strings list of Strings to get an sql array from
	 * @param preparedStatement database prepared statement to get working connection
	 * @return java sql array with pre-loaded list of strings
	 * @throws SQLException if any sql exception has been thrown
	 * @throws InternalErrorException if oracle method to work with an array can't be get or invoked
	 */
	static java.sql.Array prepareSQLArrayOfStrings(List<String> strings, PreparedStatement preparedStatement) throws SQLException {
		String[] arrayOfStrings = strings.toArray(new String[0]);
		Connection connection = preparedStatement.getConnection().unwrap(Connection.class);
		return connection.createArrayOf(JDBCType.VARCHAR.name(), arrayOfStrings);
	}

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
