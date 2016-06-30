package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import org.apache.tomcat.dbcp.dbcp.DriverManagerConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.blImpl.AttributesManagerBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;

/**
 * @author Michal Prochazka michalp@ics.muni.cz
 */
public class ExtSourceSqlComplex extends ExtSource implements ExtSourceApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourceSql.class);
	private static final Map<String, String> attributeNameMapping = new HashMap<>();
	private Connection con;
	private boolean isOracle = false;

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


	public ExtSourceSqlComplex() {
	}

		@Override
	public List<Map<String, String>> findSubjects(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		return findSubjects(searchString, 0);
	}

	@Override
	public List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		return findSubjectsLogins(searchString, maxResults);
	}

	public List<Map<String,String>> findSubjectsLogins(String searchString) throws InternalErrorException {
		return findSubjectsLogins(searchString, 0);
	}

	public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) throws InternalErrorException {
		String query = getAttributes().get("query");
		if (query == null) {
			throw new InternalErrorException("query attribute is required");
		}

		return this.querySource(query, searchString, maxResults, null);
	}

	public Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException {
		String query = getAttributes().get("loginQuery");
		if (query == null) {
			throw new InternalErrorException("loginQuery attribute is required");
		}

		List<Map<String, String>> subjects = this.querySource(query, login, 0, null);

		if (subjects.size() < 1) {
			throw new SubjectNotExistsException("Login: " + login);
		}
		if (subjects.size() > 1) {
			throw new InternalErrorException("External source must return exactly one result, search string: " + login);
		}

		return subjects.get(0);
	}

	@Override
	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		return this.getGroupSubjects(attributes, null);
	}

	@Override
	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes, List<String> logins) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		//If we want to get bulk of subjects from extSource by list of logins
		if(logins != null) {
			String bulkQuery = getAttributes().get("bulkQuery");
			if (bulkQuery == null) {
				throw new ExtSourceUnsupportedOperationException("ExtSource do not support bulkQuery in perun-extSources.xml.");
			}
			return this.querySource(bulkQuery, null, 0, logins);
		//If not, use the normal way of getting data
		} else {
			// Get the sql query for the group subjects
			String sqlQueryForGroup = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
			return this.querySource(sqlQueryForGroup, null, 0, null);
		}
	}

	protected List<Map<String,String>> querySource(String query, String searchString, int maxResults, List<String> logins) throws InternalErrorException {

		if (getAttributes().get("url") == null) {
			throw new InternalErrorException("url attribute is required");
		}

		//log.debug("Searching for '{}' using query {} in external source 'url:{}'", new Object[] {searchString, query, (String) getAttributes().get("url")});
		log.debug("Searching for '{}' in external source 'url:{}'", new Object[] {searchString, (String) getAttributes().get("url")});

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
		} catch (SQLException e) {
			log.error("SQL exception during creating connection.");
			throw new InternalErrorRuntimeException(e);
		}

		List<Map<String, String>> subjects = new ArrayList<>();
		//If logins are null, it means use the old fashion way
		if(logins == null) {
			subjects = this.normalQuery(query, maxResults, searchString);
		//If logins are not null, we want to use bulk calling
		} else {
			if(logins.size() <= AttributesManagerBlImpl.MAX_SIZE_OF_BULK_IN_SQL) subjects = bulkQuery(query, logins);
			else {
				int from = 0;
				int to = AttributesManagerBlImpl.MAX_SIZE_OF_BULK_IN_SQL;

				do {
					subjects.addAll(bulkQuery(query, logins.subList(from, to)));
					from+=AttributesManagerBlImpl.MAX_SIZE_OF_BULK_IN_SQL;
					to+=AttributesManagerBlImpl.MAX_SIZE_OF_BULK_IN_SQL;
				} while (logins.size()>to);
				subjects.addAll(bulkQuery(query, logins.subList(from, logins.size())));
			}
		}
		return subjects;
	}

	protected List<Map<String, String>> normalQuery(String query, int maxResults, String searchString) throws InternalErrorException {
		PreparedStatement st = null;
		ResultSet rs = null;

		try {
			st = this.con.prepareStatement(query);

			// Substitute the ? in the query by the seachString
			if (searchString != null && !searchString.isEmpty()) {
				for (int i = st.getParameterMetaData().getParameterCount(); i > 0; i--) {
					st.setString(i, searchString);
				}
			}

			// Limit results
			if (maxResults > 0) {
				st.setMaxRows(maxResults);
			}
			rs = st.executeQuery();

			List<Map<String, String>> subjects = new ArrayList<Map<String, String>>();

			log.trace("Query {}", query);

			while (rs.next()) {
				Map<String, String> map = new HashMap<String, String>();

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
							log.error("Unknown attribute type '{}' for user {} {}, attributeRaw {}", new Object[] {attributeRaw[0], map.get("firstName"), map.get("lastName"), attributeRaw});
						} else if (!attributeNameMapping.containsKey(attributeRaw[1])) {
							log.error("Unknown attribute type '{}' for user {} {}, attributeRaw {}", new Object[] {attributeRaw[1], map.get("firstName"), map.get("lastName"), attributeRaw});
						} else {
							attributeName = attributeNameMapping.get(attributeRaw[0]) + attributeNameMapping.get(attributeRaw[1]) + attributeRaw[2];
							log.trace("Adding attribute {} with value {}", attributeName, rs.getString(i));
						}

						String attributeValue = rs.getString(i);
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

			log.debug("Returning {} subjects from external source {} for searchString {}", new Object[] {subjects.size(), this, searchString});
			return subjects;

		} catch (SQLException e) {
			log.error("SQL exception during searching for subject '{}'", query);
			throw new InternalErrorRuntimeException(e);
		} finally {
			try {
				if (rs != null) rs.close();
				if (st != null) st.close();
			} catch (SQLException e) {
				log.error("SQL exception during closing the resultSet or statement, while searching for subject '{}'", query);
				throw new InternalErrorRuntimeException(e);
			}
		}
	}

	protected List<Map<String, String>> bulkQuery(String query, List<String> logins) throws InternalErrorException {
		PreparedStatement st = null;
		ResultSet rs = null;

		List<Map<String, String>> subjects = new ArrayList<>();

		try {
			//set (in (...) or in (...) ...) instead of "?", no identifier there
			query = query.replace("?", BeansUtils.prepareInSQLClauseForValues(logins, ""));

			st = this.con.prepareStatement(query);

			rs = st.executeQuery();

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
							log.error("Unknown attribute type '{}' for user {} {}, attributeRaw {}", new Object[] {attributeRaw[0], map.get("firstName"), map.get("lastName"), attributeRaw});
						} else if (!attributeNameMapping.containsKey(attributeRaw[1])) {
							log.error("Unknown attribute type '{}' for user {} {}, attributeRaw {}", new Object[] {attributeRaw[1], map.get("firstName"), map.get("lastName"), attributeRaw});
						} else {
							attributeName = attributeNameMapping.get(attributeRaw[0]) + attributeNameMapping.get(attributeRaw[1]) + attributeRaw[2];
							log.trace("Adding attribute {} with value {}", attributeName, rs.getString(i));
						}

						String attributeValue = rs.getString(i);
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

			log.debug("Returning {} subjects from external source {}", new Object[] {subjects.size(), this});
			return subjects;

		} catch (SQLException e) {
			log.error("SQL exception during searching for subject '{}'", query);
			throw new InternalErrorRuntimeException(e);
		} finally {
			try {
				if (rs != null) rs.close();
				if (st != null) st.close();
			} catch (SQLException e) {
				log.error("SQL exception during closing the resultSet or statement, while searching for subject '{}'", query);
				throw new InternalErrorRuntimeException(e);
			}
		}
	}

	protected void createConnection() throws SQLException {
    try {
      if (getAttributes().get("user") != null && getAttributes().get("password") != null) {
        this.con = (new DriverManagerConnectionFactory((String) getAttributes().get("url"),
            (String) getAttributes().get("user"), (String) getAttributes().get("password"))).createConnection();
      } else {
        this.con = (new DriverManagerConnectionFactory((String) getAttributes().get("url"), null)).createConnection();
      }

      // Set connection to read-only mode
      this.con.setReadOnly(true);

      if (this.con.getMetaData().getDriverName().toLowerCase().contains("oracle")) {
        this.isOracle = true;
      }
    } catch (SQLException e) {
      log.error("SQL exception during creating the connection to URL", (String) getAttributes().get("url"));
      throw new InternalErrorRuntimeException(e);
    }
  }

	public void close() throws InternalErrorException {
		if (this.con != null) {
			try {
				this.con.close();
				this.con = null;
			} catch (SQLException e) {
				throw new InternalErrorException(e);
			}
		}
	}

	protected Map<String,String> getAttributes() {
		return perunBl.getExtSourcesManagerBl().getAttributes(this);
	}
}
