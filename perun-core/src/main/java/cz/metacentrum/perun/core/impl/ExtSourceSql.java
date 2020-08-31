package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static cz.metacentrum.perun.core.blImpl.GroupsManagerBlImpl.GROUP_SYNC_DEFAULT_DATA;
import static java.util.stream.Collectors.toMap;

/**
 * @author Michal Prochazka michalp@ics.muni.cz
 */
public class ExtSourceSql extends ExtSource implements ExtSourceSimpleApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourceSql.class);
	private static final Map<String, String> attributeNameMapping = new HashMap<>();
	private Connection con;
	private boolean isOracle = false;
	private boolean isSQLite = false;

	private static PerunBlImpl perunBl;

	// filled by spring (perun-core.xml)
	public static PerunBlImpl setPerunBlImpl(PerunBlImpl perun) {
		perunBl = perun;
		return perun;
	}

	static {
		attributeNameMapping.put("m", "urn:perun:member");
		attributeNameMapping.put("u", "urn:perun:user");
		attributeNameMapping.put("f", "urn:perun:facility");
		attributeNameMapping.put("r", "urn:perun:resource");
		attributeNameMapping.put("g", "urn:perun:group");
		attributeNameMapping.put("v", "urn:perun:vo");
		attributeNameMapping.put("h", "urn:perun:host");
		attributeNameMapping.put("mr", "urn:perun:member_resource");
		attributeNameMapping.put("uf", "urn:perun:user_facility");
		attributeNameMapping.put("gr", "urn:perun:group_resource");
		attributeNameMapping.put("o", ":attribute-def:opt:");
		attributeNameMapping.put("d", ":attribute-def:def:");
	}


	public ExtSourceSql() {
	}

	@Override
	public List<Map<String,String>> findSubjectsLogins(String searchString) {
		return findSubjectsLogins(searchString, 0);
	}

	@Override
	public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) {
		String query = getAttributes().get("query");
		if (query == null) {
			throw new InternalErrorException("query attribute is required");
		}

		return this.querySource(query, searchString, maxResults);
	}

	@Override
	public Map<String, String> getSubjectByLogin(String login) throws SubjectNotExistsException {
		String query = getAttributes().get("loginQuery");
		if (query == null) {
			throw new InternalErrorException("loginQuery attribute is required");
		}

		List<Map<String, String>> subjects = this.querySource(query, login, 0);

		if (subjects.size() < 1) {
			throw new SubjectNotExistsException("Login: " + login);
		}
		if (subjects.size() > 1) {
			throw new InternalErrorException("External source must return exactly one result, search string: " + login);
		}

		return subjects.get(0);
	}

	@Override
	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) {
		// Get the sql query for the group subjects
		String sqlQueryForGroup = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);

		return this.querySource(sqlQueryForGroup, null, 0);
	}

	@Override
	public List<Map<String,String>> getUsersSubjects() {
		String query = getAttributes().get("usersQuery");

		if (query == null) {
			throw new InternalErrorException("usersQuery attribute is required");
		}

		return this.querySource(query, null, 0);
	}

	protected List<Map<String,String>> querySource(String query, String searchString, int maxResults) {
		log.debug("Searching for '{}' in external source 'url:{}'", searchString, getAttributes().get("url"));

		this.checkAndSetPrerequisites();

		try (PreparedStatement st = getPreparedStatement(query, searchString, maxResults)) {
			try (ResultSet rs = st.executeQuery()) {
				List<Map<String, String>> subjects = new ArrayList<>();

				log.trace("Query {}", query);

				while (rs.next()) {
					Map<String, String> map = new HashMap<>();

					try {
						map.put("firstName", rs.getString("firstName"));
					} catch (SQLException e) {
						// If the column doesn't exists, ignore it
						map.put("firstName", null);
					}
					try {
						map.put("lastName", rs.getString("lastName"));
					} catch (SQLException e) {
						// If the column doesn't exists, ignore it
						map.put("lastName", null);
					}
					try {
						map.put("middleName", rs.getString("middleName"));
					} catch (SQLException e) {
						// If the column doesn't exists, ignore it
						map.put("middleName", null);
					}
					try {
						map.put("titleBefore", rs.getString("titleBefore"));
					} catch (SQLException e) {
						// If the column doesn't exists, ignore it
						map.put("titleBefore", null);
					}
					try {
						map.put("titleAfter", rs.getString("titleAfter"));
					} catch (SQLException e) {
						// If the column doesn't exists, ignore it
						map.put("titleAfter", null);
					}
					try {
						map.put("login", rs.getString("login"));
					} catch (SQLException e) {
						// If the column doesn't exists, ignore it
						map.put("login", null);
					}

					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
						String columnName = rs.getMetaData().getColumnLabel(i);
						log.trace("Iterating through attribute {}", columnName);
						// Now go through all other attributes. If the column name(=attribute name) contains ":", then it represents an attribute
						if (columnName.contains(":")) {
							// Decode the attribute name (column name has limited size, so we need to code the attribute names)
							// Coded attribute name: x:y:z
							// x - m: member, u: user, f: facility, r: resource, mr: member-resource, uf: user-facility, h: host, v: vo, g: group, gr: group-resource
							// y - d: def, o: opt
							String[] attributeRaw = columnName.split(":", 3);
							String attributeName = null;
							if (!attributeNameMapping.containsKey(attributeRaw[0])) {
								log.warn("Unknown attribute type '{}' for user {} {}, attributeRaw {}", attributeRaw[0], map.get("firstName"), map.get("lastName"), attributeRaw);
							} else if (!attributeNameMapping.containsKey(attributeRaw[1])) {
								log.warn("Unknown attribute type '{}' for user {} {}, attributeRaw {}", attributeRaw[1], map.get("firstName"), map.get("lastName"), attributeRaw);
							} else {
								attributeName = attributeNameMapping.get(attributeRaw[0]) + attributeNameMapping.get(attributeRaw[1]) + attributeRaw[2];
								if (!Objects.equals(rs.getMetaData().getColumnTypeName(i), "BLOB")) {
									// trace only string data
									log.trace("Adding attribute {} with value {}", attributeName, rs.getString(i));
								} else {
									log.trace("Adding attribute {} with BLOB value", attributeName);
								}
							}
							String attributeValue = null;
							if (Objects.equals(rs.getMetaData().getColumnTypeName(i), "BLOB")) {
								// source column is binary
								attributeValue = parseBlobValue(rs.getBinaryStream(i), columnName);
							} else {
								// let driver to convert type to string
								attributeValue = rs.getString(i);
							}
							if (rs.wasNull()) {
								map.put(attributeName, null);
							} else {
								map.put(attributeName, attributeValue);
							}
						} else if (columnName.toLowerCase().startsWith(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING)) {
							// additionalUserExtSources, we must do lower case because some DBs changes lower to upper
							map.put(columnName.toLowerCase(), rs.getString(i));
							log.trace("Adding attribute {} with value {}", columnName, rs.getString(i));
						}
					}
					subjects.add(map);
				}

				log.debug("Returning {} subjects from external source {} for searchString {}", subjects.size(), this, searchString);
				return subjects;
			}
		} catch (SQLException e) {
			log.error("SQL exception during searching for subject '{}'", query);
			throw new InternalErrorException(e);
		}
	}

	protected void createConnection() {
		try {

			String connectionUrl = getAttributes().get("url");
			String user = getAttributes().get("user");
			String pass = getAttributes().get("password");
			Properties connectionProperties = new Properties();

			// set user/pass to properties if present
			if (user != null && pass != null) {
				connectionProperties.put("user", user);
				connectionProperties.put("password", pass);
			}

			// set connection read_only for SQLite (doesn't follow JDBC standard)
			if (connectionUrl.startsWith("jdbc:sqlite:")) {
				isSQLite = true;
				connectionProperties.put("open_mode","1");
			}

			// create connection
			this.con = DriverManager.getConnection(connectionUrl, connectionProperties);

			// Set connection to read-only mode for standard JDBC drivers
			if (!isSQLite) {
				this.con.setReadOnly(true);
			}

			if (this.con.getMetaData().getDriverName().toLowerCase().contains("oracle")) {
				this.isOracle = true;
				this.isSQLite = false;
			}

		} catch (SQLException e) {
			log.error("SQL exception during creating the connection to URL", getAttributes().get("url"));
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void close() {
		if (this.con != null) {
			try {
				this.con.close();
				this.con = null;
			} catch (SQLException e) {
				throw new InternalErrorException(e);
			}
		}
	}

	@Override
	public List<Map<String, String>> getSubjectGroups(Map<String, String> attributes) throws ExtSourceUnsupportedOperationException {
		String sqlQueryForGroup = attributes.get(GroupsManager.GROUPSQUERY_ATTRNAME);

		return this.groupQuery(sqlQueryForGroup, null, 0);
	}

	/**
	 * Get subject groups from an external source
	 *
	 * @param query to select subject groups
	 * @param searchString by which will be ? in query replaced
	 * @param maxResults maximum subjects to get
	 * @return list of subjects
	 * @throws InternalErrorException
	 */
	protected List<Map<String,String>> groupQuery(String query, String searchString, int maxResults) {

		this.checkAndSetPrerequisites();

		try (PreparedStatement st = getPreparedStatement(query, searchString, maxResults)) {
			try (ResultSet rs = st.executeQuery()) {

				List<Map<String, String>> subjects = new ArrayList<>();

				log.trace("Query {}", query);

				while (rs.next()) {
					Map<String, String> map = new HashMap<>();
					GROUP_SYNC_DEFAULT_DATA.forEach(
						column -> map.put(column, readGroupSyncRequiredData(rs, column))
					);
					map.putAll(parseAdditionalAttributeData(rs));
					subjects.add(map);
				}

				log.debug("Returning {} subjects from external source {} for searchString {}", subjects.size(), this, searchString);
				return subjects;
			}
		} catch (SQLException e) {
			log.error("SQL exception during searching for subject '{}'", query);
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Decodes a full attribute name from the given value. For used mappings,
	 * see attributeNameMapping.
	 *
	 * @param value which will be mapped
	 * @return attribute full name
	 */
	private String mapAttributeNames(String value) {
		String[] attributeRaw = value.split(":", 3);
		String attributeName = null;
		if (!attributeNameMapping.containsKey(attributeRaw[0])) {
			log.warn("Unknown attribute type '{}', attributeRaw {}", attributeRaw[0], attributeRaw);
		} else if (!attributeNameMapping.containsKey(attributeRaw[1])) {
			log.warn("Unknown attribute type '{}', attributeRaw {}", attributeRaw[1], attributeRaw);
		} else {
			attributeName = attributeNameMapping.get(attributeRaw[0]) + attributeNameMapping.get(attributeRaw[1]) +
					attributeRaw[2];
		}
		return attributeName;
	}

	/**
	 * Parse additional data from the given result set.
	 *
	 * @param rs result set with attribute data
	 * @throws SQLException SQLException
	 */
	private Map<String, String> parseAdditionalAttributeData(ResultSet rs) throws SQLException {
		Map<String, String> additionalAttributes = new HashMap<>();
		for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
			String columnName = rs.getMetaData().getColumnLabel(i);
			if (columnName.contains(":")) {
				String attrName = mapAttributeNames(columnName);
				String attributeValue = null;
				if (Objects.equals(rs.getMetaData().getColumnTypeName(i), "BLOB")) {
					attributeValue = parseBlobValue(rs.getBinaryStream(i), columnName);
				} else {
					// let driver to convert type to string
					attributeValue = rs.getString(i);
				}
				additionalAttributes.put(attrName, attributeValue);
			}
		}
		return additionalAttributes;
	}

	/**
	 * Parse blob value from given input stream.
	 *
	 * @param inputStream input stream with a blob value
	 * @param columnName name of the column to which the blob belongs
	 * @return parsed value
	 */
	private String parseBlobValue(InputStream inputStream, String columnName) {
		if (inputStream == null) {
			return null;
		}
		try {
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
			byte[] bytes = Base64.encodeBase64(result.toByteArray());
			return new String(bytes, StandardCharsets.UTF_8);
		} catch (IOException ex) {
			log.error("Unable to read BLOB for column {}", columnName);
			throw new InternalErrorException("Unable to read BLOB data for column: " + columnName, ex);
		}
	}

	/**
	 * Read data from rs from specified column. If the column doesn't exist,
	 * a null is returned.
	 *
	 * @param rs result rest
	 * @param column column
	 * @return column data or null if column doesn't exist
	 */
	private String readGroupSyncRequiredData(ResultSet rs, String column) {
		try {
			return rs.getString(column);
		} catch (SQLException e) {
			// If the column doesn't exists, ignore it
			return null;
		}
	}

	protected Map<String,String> getAttributes() {
		return perunBl.getExtSourcesManagerBl().getAttributes(this);
	}

	/**
	 * Check if needed prerequisites are set to be able to call query.
	 *
	 * @throws InternalErrorException if expected attributes are not set
	 */
	private void checkAndSetPrerequisites() {
		if (getAttributes().get("url") == null) {
			throw new InternalErrorException("url attribute is required");
		}

		// Register driver if the attribute has been defined
		if (getAttributes().get("driver") != null) {
			try {
				Class.forName(getAttributes().get("driver"));
			} catch (ClassNotFoundException e) {
				throw new InternalErrorException("Driver " + getAttributes().get("driver") + " cannot be registered", e);
			}
		}

		try {
			// Check if we have existing connection. In case of Oracle also checks the connection validity
			if (this.con == null || (this.isOracle && !this.con.isValid(0))) {
				this.createConnection();
			}
		} catch (SQLException ex) {
			throw new InternalErrorException("Can't check if connection to database is still valid in SQL ExtSource.", ex);
		}
	}

	/**
	 * Prepares query statement from query.
	 * - substitutes character '?' by searchString.
	 * - set max results limit.
	 *
	 * @param query basic query
	 * @param searchString search string
	 * @param maxResults limit of max results where 0 is unlimited
	 * @return
	 * @throws SQLException
	 */
	protected PreparedStatement getPreparedStatement(String query, String searchString, int maxResults) throws SQLException {
		PreparedStatement st = this.con.prepareStatement(query);

		// Substitute the ? in the query by the searchString
		if (searchString != null && !searchString.isEmpty()) {
			for (int i = st.getParameterMetaData().getParameterCount(); i > 0; i--) {
				st.setString(i, searchString);
			}
		}

		// Limit results
		if (maxResults > 0) {
			st.setMaxRows(maxResults);

		}

		return st;
	}
}
