package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.implApi.SearcherImplApi;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Searcher Class for searching objects by Map of Attributes
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class SearcherImpl implements SearcherImplApi {

  private static final Logger LOG = LoggerFactory.getLogger(SearcherImpl.class);
  private static final Map<String, Map<String, String>> ENTITY_ATTR_TABLE_NAMES = Map.of(
      "facility", Map.of("attrValueTableName", "facility_attr_values", "entityTableName", "facilities"),
      "group", Map.of("attrValueTableName", "group_attr_values", "entityTableName", "groups"),
      "user", Map.of("attrValueTableName", "user_attr_values", "entityTableName", "users"),
      "resource", Map.of("attrValueTableName", "resource_attr_values", "entityTableName", "resources"),
      "member", Map.of("attrValueTableName", "member_attr_values", "entityTableName", "members")
  );

  private static NamedParameterJdbcTemplate jdbc;
  private static JdbcPerunTemplate jdbcTemplate;

  public SearcherImpl(DataSource perunPool) {
    jdbc = new NamedParameterJdbcTemplate(perunPool);
    jdbcTemplate = new JdbcPerunTemplate(perunPool);
    jdbc.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
    jdbcTemplate.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
  }

  @Override
  public List<Facility> getFacilities(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues)
      throws WrongAttributeValueException {
    StringBuilder query = new StringBuilder();
    query.append("select distinct " + FacilitiesManagerImpl.FACILITY_MAPPING_SELECT_QUERY + " from facilities ");

    MapSqlParameterSource parameters = new MapSqlParameterSource();

    insertWhereClausesAndQueryParametersFromAttributes(query, parameters, "facility_attr_values", "facility",
        "facilities", attributesWithSearchingValues, false);

    try {
      return jdbc.query(query.toString(), parameters, FacilitiesManagerImpl.FACILITY_MAPPER);
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Group> getGroups(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues)
      throws WrongAttributeValueException {
    StringBuilder query = new StringBuilder();
    query.append("select distinct " + GroupsManagerImpl.GROUP_MAPPING_SELECT_QUERY + " from groups ");

    MapSqlParameterSource parameters = new MapSqlParameterSource();

    insertWhereClausesAndQueryParametersFromAttributes(query, parameters, "group_attr_values", "group", "groups",
        attributesWithSearchingValues, false);

    try {
      return jdbc.query(query.toString(), parameters, GroupsManagerImpl.GROUP_MAPPER);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Group> getGroupsByGroupResourceSetting(PerunSession sess, Attribute groupResourceAttribute,
                                                     Attribute resourceAttribute) {
    try {
      return jdbcTemplate.query(
          "select  " + GroupsManagerImpl.GROUP_MAPPING_SELECT_QUERY + " from groups where groups.id in ( " +
          "select distinct gr.group_id from groups_resources gr " +
          "join group_resource_attr_values grav on gr.group_id=grav.group_id and gr.resource_id=grav.resource_id " +
          "join resource_attr_values rav on gr.resource_id=rav.resource_id " +
          "where rav.attr_id=? and grav.attr_id=? and rav.attr_value=? and grav.attr_value=?)",
          GroupsManagerImpl.GROUP_MAPPER, resourceAttribute.getId(), groupResourceAttribute.getId(),
          resourceAttribute.getValue(), groupResourceAttribute.getValue());
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);

    }
  }

  @Override
  public List<Integer> getGroupsIdsForAppAutoRejection() {
    String sql = "select distinct groups.id from groups join application on groups.id=application.group_id join " +
                 "group_attr_values " +
                 "on groups.id=group_attr_values.group_id join attr_names on attr_names.id=group_attr_values.attr_id " +
                 "where application.state in ('NEW', 'VERIFIED') and (attr_names.friendly_name= " +
                 "'applicationExpirationRules' " + "and attr_names.namespace= 'urn:perun:group:attribute-def:def')";
    return jdbcTemplate.queryForList(sql, Integer.class);
  }

  @Override
  public List<Member> getMembersByExpiration(PerunSession sess, String operator, LocalDate date, int days) {

    // this would default to now
    if (date == null) {
      date = LocalDate.now();
    }
    date = date.plusDays(days);
    // create sql toDate()
    String compareDate = "TO_DATE('" + date + "','YYYY-MM-DD')";

    if (operator == null || operator.isEmpty()) {
      operator = "=";
    }

    if (!operator.equals("<") && !operator.equals("<=") && !operator.equals("=") && !operator.equals(">=") &&
        !operator.equals(">")) {
      throw new InternalErrorException("Operator '" + operator + "' is not allowed in SQL.");
    }

    String query = "select distinct " +
                       MembersManagerImpl.MEMBER_MAPPING_SELECT_QUERY + " from member_attr_values val" +
                   " join members on val.member_id=members.id" + " and val.attr_id=(select id from attr_names where " +
                   "attr_name='urn:perun:member:attribute-def:def:membershipExpiration')" +
                   " and TO_DATE(val.attr_value, 'YYYY-MM-DD')" + operator + compareDate;
    try {

      return jdbcTemplate.query(query, MembersManagerImpl.MEMBER_MAPPER);

    } catch (Exception e) {
      LOG.error("Failed to get all vos members by expiration using query: {}", query);
      throw new InternalErrorException(e);
    }

  }

  @Override
  public List<Member> getMembersByGroupExpiration(PerunSession sess, Group group, String operator, LocalDate date,
                                                  int days) {

    // if date is null, use today
    if (date == null) {
      date = LocalDate.now();
    }
    date = date.plusDays(days);
    // create sql toDate()
    String compareDate = "TO_DATE('" + date + "','YYYY-MM-DD')";

    if (operator == null || operator.isEmpty()) {
      operator = "=";
    }

    if (!operator.equals("<") && !operator.equals("<=") && !operator.equals("=") && !operator.equals(">=") &&
        !operator.equals(">")) {
      throw new InternalErrorException("Operator '" + operator + "' is not allowed in SQL.");
    }

    // Use Oracle specific SQL hint to force usage of attr_id index to prevent to_date() failure on invalid input
    String query = "select /*+INDEX(val IDX_FK_MEMGAV_ACCATTNAM) */ distinct " +
                   MembersManagerImpl.GROUPS_MEMBERS_MAPPING_SELECT_QUERY + " from member_group_attr_values val" +
                   " join groups_members on val.member_id=groups_members.member_id" +
                   " and val.group_id=groups_members.group_id" + " and val.group_id=?" +
                   " and val.attr_id=(select id from attr_names where " +
                   "attr_name='urn:perun:member_group:attribute-def:def:groupMembershipExpiration')" +
                   " and TO_DATE(val.attr_value, 'YYYY-MM-DD')" + operator + compareDate +
                   " join members on members.id=val.member_id";
    try {

      return jdbcTemplate.query(query, MembersManagerImpl.MEMBERS_WITH_GROUP_STATUSES_SET_EXTRACTOR, group.getId());

    } catch (Exception e) {
      LOG.error("Failed to get group members of {} by expiration using query: {}", group, query);
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Resource> getResources(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues,
                                     boolean allowPartialMatchForString) throws WrongAttributeValueException {
    StringBuilder query = new StringBuilder();
    query.append("select distinct " + ResourcesManagerImpl.RESOURCE_MAPPING_SELECT_QUERY + " from resources ");

    MapSqlParameterSource parameters = new MapSqlParameterSource();

    insertWhereClausesAndQueryParametersFromAttributes(query, parameters, "resource_attr_values", "resource",
        "resources", attributesWithSearchingValues, allowPartialMatchForString);

    try {
      return jdbc.query(query.toString(), parameters, ResourcesManagerImpl.RESOURCE_MAPPER);
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<User> getUsers(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues)
      throws WrongAttributeValueException {
    StringBuilder query = new StringBuilder();
    query.append("select distinct " + UsersManagerImpl.USER_MAPPING_SELECT_QUERY + " from users ");

    MapSqlParameterSource parameters = new MapSqlParameterSource();

    insertWhereClausesAndQueryParametersFromAttributes(query, parameters, "user_attr_values", "user", "users",
        attributesWithSearchingValues, false);

    try {
      return jdbc.query(query.toString(), parameters, UsersManagerImpl.USER_MAPPER);
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Member> getMembers(PerunSession sess, Vo vo,
                                 Map<String, Map<Attribute, String>> mapOfEntityToMapOfAttrsWithValues)
      throws WrongAttributeValueException {
    StringBuilder query = new StringBuilder();
    query.append("select distinct " + MembersManagerImpl.MEMBER_MAPPING_SELECT_QUERY + " from " +
                     "(select * from members where members.vo_id=").append(vo.getId()).append(") members ")
        .append("join users on users.id=members.user_id ");

    MapSqlParameterSource parameters = new MapSqlParameterSource();

    insertWhereClausesAndQueryParametersFromMixedEntitiesAttributes(query, parameters,
        mapOfEntityToMapOfAttrsWithValues, false);

    try {
      return jdbc.query(query.toString(), parameters, MembersManagerImpl.MEMBER_MAPPER);
    } catch (EmptyResultDataAccessException e) {
      return new ArrayList<>();
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Integer> getVosIdsForAppAutoRejection() {
    String sql = "select distinct vos.id from vos join application on vos.id=application.vo_id join vo_attr_values " +
                 "on vos.id=vo_attr_values.vo_id join attr_names on attr_names.id=vo_attr_values.attr_id " +
                 "where application.state in ('NEW', 'VERIFIED') and (attr_names.friendly_name= " +
                 "'applicationExpirationRules' " +
                 "and attr_names.namespace= 'urn:perun:vo:attribute-def:def')";
    return jdbcTemplate.queryForList(sql, Integer.class);
  }

  /**
   * Generates into given query 'WHERE' clauses based on values from given Map. Into given parameters adds objects
   * needed in generated clauses.
   *
   * @param query                         output where are the generated clauses appended
   * @param parameters                    output where are added objects used in where clauses
   * @param attributesWithSearchingValues attributes with values used for generating WHERE clauses
   * @param allowPartialMatchForString    if false, search only by exact match, if true, search also by partial match
   *                                      (for String values only!)
   * @throws InternalErrorException internal error
   * @throws WrongAttributeValueException wrong attribute value
   */
  @SuppressWarnings("ConstantConditions")
  private void insertWhereClausesAndQueryParametersFromAttributes(StringBuilder query, MapSqlParameterSource parameters,
                                                                  String attrValueTableName, String entityName,
                                                                  String entityTableName,
                                                                  Map<Attribute, String> attributesWithSearchingValues,
                                                                  boolean allowPartialMatchForString)
      throws WrongAttributeValueException {
    List<String> whereClauses = new ArrayList<>();
    int counter = 0;
    for (Attribute key : attributesWithSearchingValues.keySet()) {
      counter++;
      String value = attributesWithSearchingValues.get(key);
      extendQueryWithAttribute(query, parameters, attrValueTableName, entityName, entityTableName,
          value, allowPartialMatchForString, key, counter, whereClauses);
    }

    //Add Where clauses at end of sql query
    boolean first = true;
    for (String whereClause : whereClauses) {
      if (first) {
        query.append("where ");
        query.append(whereClause);
        first = false;
      } else {
        query.append("and ");
        query.append(whereClause);
      }
    }
  }

  /**
   * Generates 'WHERE' clauses into given query based on values from given Map. Works with a map of entities to
   * attributes with searching values. The entity name is used to look up the corresponding tables for the attributes
   * we want to search by.
   *
   * @param query                                 output where are the generated clauses appended
   * @param parameters                            output where are added objects used in where clauses
   * @param entityToAttributesWithSearchingValues top level keys are entities (namespaces) mapping to attributes with
   *                                              values in these namespaces
   * @param allowPartialMatchForString            if false, search only by exact match,
   *                                              if true, search also by partial match (for String values only!)
   * @throws InternalErrorException internal error
   * @throws WrongAttributeValueException wrong attribute value
   */
  @SuppressWarnings("ConstantConditions")
  private void insertWhereClausesAndQueryParametersFromMixedEntitiesAttributes(
      StringBuilder query, MapSqlParameterSource parameters,
      Map<String, Map<Attribute, String>> entityToAttributesWithSearchingValues, boolean allowPartialMatchForString)
      throws WrongAttributeValueException {
    List<String> whereClauses = new ArrayList<>();
    int counter = 0;
    for (String entity : entityToAttributesWithSearchingValues.keySet()) {
      for (Attribute key : entityToAttributesWithSearchingValues.get(entity).keySet()) {
        counter++;
        String value = entityToAttributesWithSearchingValues.get(entity).get(key);
        String attrValueTableName = ENTITY_ATTR_TABLE_NAMES.get(entity).get("attrValueTableName");
        String entityTableName = ENTITY_ATTR_TABLE_NAMES.get(entity).get("entityTableName");
        extendQueryWithAttribute(query, parameters, attrValueTableName, entity, entityTableName,
            value, allowPartialMatchForString, key, counter, whereClauses);
      }
    }

    boolean first = true;
    for (String whereClause : whereClauses) {
      if (first) {
        query.append("where ");
        query.append(whereClause);
        first = false;
      } else {
        query.append("and ");
        query.append(whereClause);
      }
    }
  }


  /**
   * Take an existing query searching for entities by values of specific attributes and generate an extension of the
   * WHERE clause by adding another attribute and value to search by.
   *
   * @param query partially built query
   * @param parameters SQL parameters to extend
   * @param attrValueTableName corresponding table name of the attribute we are searching by
   * @param entityName name of the entity we are searching for
   * @param entityTableName corresponding table name of the entity we are searching for
   * @param value value of the attribute we are searching by
   * @param allowPartialMatchForString search also by partial match
   * @param attr attribute object by which we are searching
   * @param counter the order of the where clause being generated in this method for the query
   * @param whereClauses the previously generated where clauses for the query
   * @throws WrongAttributeValueException wrong attribute value
   */
  private void extendQueryWithAttribute(StringBuilder query, MapSqlParameterSource parameters,
                                String attrValueTableName, String entityName, String entityTableName,
                                String value, boolean allowPartialMatchForString, Attribute attr, int counter,
                                List<String> whereClauses) throws WrongAttributeValueException {
    query.append("left join ").append(attrValueTableName).append(" val").append(counter).append(" ");
    query.append("on val").append(counter).append(".").append(entityName).append("_id=").append(entityTableName)
        .append(".id and val").append(counter).append(".attr_id=").append(attr.getId()).append(" ");
    query.append("left join attr_names nam").append(counter).append(" on val").append(counter).append(".attr_id=nam")
        .append(counter).append(".id ");

    if (value == null || value.isEmpty()) {
      whereClauses.add("val" + counter + ".attr_value IS NULL ");
    } else {
      if (attr.getType().equals(Integer.class.getName())) {
        try {
          attr.setValue(Integer.valueOf(value));
          whereClauses.add("val" + counter + ".attr_value=:v" + counter + " ");
          whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
          parameters.addValue("n" + counter, Integer.class.getName());
          parameters.addValue("v" + counter, BeansUtils.attributeValueToString(attr));
        } catch (NumberFormatException ex) {
          throw new WrongAttributeValueException(
              "Searched value for attribute: " + attr + " should be type of Integer");
        }
      } else if (attr.getType().equals(String.class.getName())) {
        attr.setValue(value);
        if (allowPartialMatchForString) {
          whereClauses.add("lower(" + Compatibility.convertToAscii("val" + counter + ".attr_value") +
                           ") LIKE CONCAT('%', CONCAT(lower(" + Compatibility.convertToAscii(":v" + counter) +
                           "), '%')) ");
        } else {
          whereClauses.add("lower(" + Compatibility.convertToAscii("val" + counter + ".attr_value") + ")=lower(" +
                           Compatibility.convertToAscii(":v" + counter) + ") ");
        }
        whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
        parameters.addValue("n" + counter, attr.getType());
        parameters.addValue("v" + counter, BeansUtils.attributeValueToString(attr));
      } else if (attr.getType().equals(Boolean.class.getName())) {
        if (!value.equals("false") && !value.equals("true")) {
          throw new WrongAttributeValueException(
              "Searched value for attribute: " + attr + " should be 'true' or 'false'");
        }
        attr.setValue(Boolean.valueOf(value));
        whereClauses.add("lower(" + Compatibility.convertToAscii("val" + counter + ".attr_value") + ")=lower(" +
                         Compatibility.convertToAscii(":v" + counter) + ") ");
        whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
        parameters.addValue("n" + counter, Boolean.class.getName());
        parameters.addValue("v" + counter, BeansUtils.attributeValueToString(attr));
      } else if (attr.getType().equals(ArrayList.class.getName())) {
        List<String> list = new ArrayList<>();
        list.add(value);
        attr.setValue(list);
        whereClauses.add("val" + counter + ".attr_value LIKE :v" + counter + " ");
        whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
        parameters.addValue("n" + counter, attr.getType());
        // key can not be null because value is not null due to previous check
        parameters.addValue("v" + counter, '%' + BeansUtils.attributeValueToString(attr)
            .substring(0, BeansUtils.attributeValueToString(attr).length() - 1) + '%');
      } else if (attr.getType().equals(LinkedHashMap.class.getName())) {
        String[] splitMapItem = value.split("=");
        if (splitMapItem.length == 0) {
          throw new InternalErrorException("Value can't be split by char '='.");
        }
        String splitKey = splitMapItem[0];
        StringBuilder splitValue = new StringBuilder();
        if (splitMapItem.length > 1) {
          for (int i = 1; i < splitMapItem.length; i++) {
            if (i != 1) {
              splitValue.append('=');
            }
            splitValue.append(splitMapItem[i]);
          }
        }
        Map<String, String> map = new LinkedHashMap<>();
        map.put(splitKey, splitValue.length() == 0 ? null : splitValue.toString());
        attr.setValue(map);
        whereClauses.add(
            "val" + counter + ".attr_value LIKE :v" + counter + " or val" + counter + ".attr_value LIKE :vv" +
                counter + " ");
        whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
        parameters.addValue("n" + counter, LinkedHashMap.class.getName());
        parameters.addValue("v" + counter, BeansUtils.attributeValueToString(attr) + '%');
        parameters.addValue("vv" + counter, "%," + BeansUtils.attributeValueToString(attr) + '%');
      } else {
        throw new InternalErrorException(attr + " is not type of integer, string, boolean, array or hashmap.");
      }
    }
  }
}
