/**
 * 
 */
package cz.metacentrum.perun.core.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.dbcp.dbcp.DriverManagerConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;

/**
 * @author Michal Prochazka michalp@ics.muni.cz
 * @version $Id$
 */
public class ExtSourceSql extends ExtSource implements ExtSourceApi {

  private final static Logger log = LoggerFactory.getLogger(ExtSourceSql.class);
  private static Map<String, String> attributeNameMapping;
  private Connection con;

  public ExtSourceSql() {
    attributeNameMapping = new HashMap<String, String>();
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

  public List<Map<String,String>> findSubjects(String searchString) throws InternalErrorException {
    return findSubjects(searchString, 0);
  }

  public List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException {
    String query = getAttributes().get("query");
    if (query == null) {
      throw new InternalErrorException("query attribute is required");
    }

    return this.querySource(query, searchString, maxResults);
  }

  public Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException {
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

  public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException {
    // Get the sql query for the group subjects
    String sqlQueryForGroup = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);

    return this.querySource(sqlQueryForGroup, null, 0);
  }

  protected List<Map<String,String>> querySource(String query, String searchString, int maxResults) throws InternalErrorException {
    PreparedStatement st = null;
    ResultSet rs = null;

    if (getAttributes().get("url") == null) {
      throw new InternalErrorException("url attribute is required");
    }
    //log.debug("Searching for '{}' using query {} in external source 'url:{}'", new Object[] {searchString, query, (String) getAttributes().get("url")});
    log.debug("Searching for '{}' in external source 'url:{}'", new Object[] {searchString, (String) getAttributes().get("url")});

    try {
      if (this.con == null || (Compatibility.isOracle() && !this.con.isValid(0))) {
        if (getAttributes().get("user") != null && getAttributes().get("password") != null) {
          this.con = (new DriverManagerConnectionFactory((String) getAttributes().get("url"), 
              (String) getAttributes().get("user"), (String) getAttributes().get("password"))).createConnection();
        } else {
          this.con = (new DriverManagerConnectionFactory((String) getAttributes().get("url"), null)).createConnection();
        }
      }

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
          String columnName = rs.getMetaData().getColumnName(i);
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
}
