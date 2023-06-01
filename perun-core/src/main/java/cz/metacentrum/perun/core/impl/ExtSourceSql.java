package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.blImpl.GroupsManagerBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cz.metacentrum.perun.core.blImpl.GroupsManagerBlImpl.GROUP_DESCRIPTION;
import static cz.metacentrum.perun.core.blImpl.GroupsManagerBlImpl.GROUP_NAME;
import static cz.metacentrum.perun.core.blImpl.GroupsManagerBlImpl.GROUP_SYNC_DEFAULT_DATA;
import static cz.metacentrum.perun.core.blImpl.GroupsManagerBlImpl.PARENT_GROUP_LOGIN;
import static java.util.stream.Collectors.toMap;

/**
 * @author Michal Prochazka michalp@ics.muni.cz
 */
public class ExtSourceSql extends ExtSourceImpl implements ExtSourceSimpleApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourceSql.class);
	private static final Map<String, String> ATTRIBUTE_NAME_MAPPING = new HashMap<>();
	public static final String DBPOOL = "dbpool";
	public static final String USER = "user";
	public static final String URL = "url";

	private DataSource dataSource;

	static {
		ATTRIBUTE_NAME_MAPPING.put("m", "urn:perun:member");
		ATTRIBUTE_NAME_MAPPING.put("u", "urn:perun:user");
		ATTRIBUTE_NAME_MAPPING.put("f", "urn:perun:facility");
		ATTRIBUTE_NAME_MAPPING.put("r", "urn:perun:resource");
		ATTRIBUTE_NAME_MAPPING.put("g", "urn:perun:group");
		ATTRIBUTE_NAME_MAPPING.put("v", "urn:perun:vo");
		ATTRIBUTE_NAME_MAPPING.put("h", "urn:perun:host");
		ATTRIBUTE_NAME_MAPPING.put("mr", "urn:perun:member_resource");
		ATTRIBUTE_NAME_MAPPING.put("uf", "urn:perun:user_facility");
		ATTRIBUTE_NAME_MAPPING.put("gr", "urn:perun:group_resource");
		ATTRIBUTE_NAME_MAPPING.put("o", ":attribute-def:opt:");
		ATTRIBUTE_NAME_MAPPING.put("d", ":attribute-def:def:");
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
	public List<Map<String, String>> getGroupSubjects(Map<String, String> groupAttributes) throws InternalErrorException {
		// Get the sql query for the group subjects
		String sqlQueryForGroup = groupAttributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
		return this.querySource(sqlQueryForGroup, null, 0);
	}

	@Override
	public List<Map<String,String>> getUsersSubjects() throws InternalErrorException, ExtSourceUnsupportedOperationException{
		String query = getAttributes().get("usersQuery");
		if (query == null) {
			throw new InternalErrorException("usersQuery attribute is required");
		}
		return this.querySource(query, null, 0);
	}

	/**
	 * Helper structure for holding data about database column mappings.
	 */
	private static class ColumnMapping {
		int columnIndex;
		String attributeName;
		boolean blob;

		public ColumnMapping(int columnIndex, String attributeName, boolean blob) {
			this.columnIndex = columnIndex;
			this.attributeName = attributeName;
			this.blob = blob;
		}
	}

	// database columns for base attributes
	private static final String[] BASE_COLUMNS = new String[]{"login", "firstName", "lastName", "middleName", "titleBefore", "titleAfter"};
	private final static String[] GROUP_COLUMNS = new String[]{GroupsManagerBlImpl.GROUP_LOGIN, GroupsManagerBlImpl.GROUP_NAME, GroupsManagerBlImpl.PARENT_GROUP_LOGIN, GroupsManagerBlImpl.GROUP_DESCRIPTION};
	// column name should be matched case-insensitively
	private static String matchingBaseColumnName(String s) {
		for (String baseName : BASE_COLUMNS) {
			if (baseName.equalsIgnoreCase(s)) {
				return baseName;
			}
		}
		return null;
	}

	private static String matchingGroupColumnName(String s) {
		for (String baseName : GROUP_COLUMNS) {
			if (baseName.equalsIgnoreCase(s)) {
				return baseName;
			}
		}
		return null;
	}

	protected List<Map<String,String>> querySource(String query, String searchString, int maxResults) throws InternalErrorException {
		log.debug("Searching for '{}' in external source '{}'", searchString, getName());
		try (Connection con = getDataSource().getConnection()) {
			try (PreparedStatement st = con.prepareStatement(query)) {
				// Substitute the ? in the query by the searchString
				if (StringUtils.isNotBlank(searchString)) {
					for (int i = st.getParameterMetaData().getParameterCount(); i > 0; i--) {
						st.setString(i, searchString);
					}
				}
				// Limit results
				if (maxResults > 0) {
					st.setMaxRows(maxResults);
				}
				// make the SQL query
				log.trace("Query {}", query);
				try (ResultSet rs = st.executeQuery()) {
					// pre-process column metadata into columnMappings
					ResultSetMetaData metaData = rs.getMetaData();
					List<ColumnMapping> columnMappings = new ArrayList<>(metaData.getColumnCount());
					for (int i = 1; i <= metaData.getColumnCount(); i++) {
						String columnName = metaData.getColumnLabel(i);
						String baseName = matchingBaseColumnName(columnName);
						if (baseName != null) {
							columnMappings.add(new ColumnMapping(i, baseName, false));
						} else if (columnName.contains(":")) {
							// Decode the attribute name (column name has limited size, so we need to code the attribute names)
							// Coded attribute name: x:y:z
							// x - m: member, u: user, f: facility, r: resource, mr: member-resource, uf: user-facility, h: host, v: vo, g: group, gr: group-resource
							// y - d: def, o: opt
							String[] attributeRaw = columnName.split(":", 3);
							if (!ATTRIBUTE_NAME_MAPPING.containsKey(attributeRaw[0])) {
								log.warn("Unknown attribute type '{}', column {}", attributeRaw[0], columnName);
							} else if (!ATTRIBUTE_NAME_MAPPING.containsKey(attributeRaw[1])) {
								log.warn("Unknown attribute type '{}', column {}", attributeRaw[1], columnName);
							} else {
								String attributeName = ATTRIBUTE_NAME_MAPPING.get(attributeRaw[0]) + ATTRIBUTE_NAME_MAPPING.get(attributeRaw[1]) + attributeRaw[2];
								boolean blob = "BLOB".equals(metaData.getColumnTypeName(i));
								columnMappings.add(new ColumnMapping(i, attributeName, blob));
							}
						} else if (columnName.toLowerCase().startsWith(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING)) {
							// additionalUserExtSources, we must do lower case because some DBs changes lower to upper
							columnMappings.add(new ColumnMapping(i, columnName.toLowerCase(), false));
						}
					}
					// process each row
					List<Map<String, String>> subjects = new ArrayList<>();
					while (rs.next()) {
						Map<String, String> map = new HashMap<>();
						for (ColumnMapping columnMapping : columnMappings) {
							if (columnMapping.blob) {
								try (InputStream in = rs.getBinaryStream(columnMapping.columnIndex)) {
									map.put(columnMapping.attributeName, Base64.encodeBase64String(StreamUtils.copyToByteArray(in)));
								} catch (IOException ex) {
									throw new InternalErrorException("Unable to read BLOB data for column: " + columnMapping.attributeName, ex);
								}
							} else {
								map.put(columnMapping.attributeName, rs.getString(columnMapping.columnIndex));
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
		} catch (SQLException e) {
			log.error("Cannot get connection from pool",e);
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void close() {
		// no-op
	}

	@Override
	public List<Map<String, String>> getSubjectGroups(Map<String, String> groupAttributes) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		String sqlQueryForGroup = groupAttributes.get(GroupsManager.GROUPSQUERY_ATTRNAME);
		return this.groupQuery(sqlQueryForGroup);
	}

	/**
	 * Get subject groups from an external source
	 *
	 * @param query to select subject groups
	 * @return list of subjects
	 */
	protected List<Map<String,String>> groupQuery(String query) {
		try (Connection con = getDataSource().getConnection()) {
			try (PreparedStatement st = con.prepareStatement(query)) {
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

					log.debug("Returning {} subjects from external source {}", subjects.size(), this);
					return subjects;
				} catch (SQLException e) {
					log.error("SQL exception during searching for subject '{}'", query);
					throw new InternalErrorException(e);
				}
			} catch (SQLException e) {
				log.error("SQL exception during the preparation of query statement '{}'", query);
				throw new InternalErrorException(e);
			}
		} catch (SQLException e) {
			log.error("Cannot get connection from pool", e);
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
		if (!ATTRIBUTE_NAME_MAPPING.containsKey(attributeRaw[0])) {
			log.warn("Unknown attribute type '{}', attributeRaw {}", attributeRaw[0], attributeRaw);
		} else if (!ATTRIBUTE_NAME_MAPPING.containsKey(attributeRaw[1])) {
			log.warn("Unknown attribute type '{}', attributeRaw {}", attributeRaw[1], attributeRaw);
		} else {
			attributeName = ATTRIBUTE_NAME_MAPPING.get(attributeRaw[0]) + ATTRIBUTE_NAME_MAPPING.get(attributeRaw[1]) +
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
				String attributeValue;
				if (Objects.equals(rs.getMetaData().getColumnTypeName(i), "BLOB")) {
					attributeValue = parseBlobValue(rs.getBinaryStream(i), columnName);
				} else {
					// let driver convert the type to string
					attributeValue = rs.getString(i);
				}
				additionalAttributes.put(attrName, attributeValue);
			}
		}
		return additionalAttributes;
	}

	protected DataSource getDataSource() {
		if (dataSource == null) {
			Map<String, String> attributes = this.getAttributes();
			String dbpool = attributes.get(DBPOOL);
			if (dbpool == null) {
				dbpool = "db-" + attributes.get(USER) + "-" + attributes.get(URL);
			}
			this.dataSource = perunBl.getExtSourcesManagerBl().getDataSource(dbpool);
			log.debug("ExtSource {} got dbpool {}", getName(), dbpool);
		}
		return dataSource;
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
		} catch(IOException ex) {
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
			// If the column doesn't exist, ignore it
			return null;
		}
	}

}
