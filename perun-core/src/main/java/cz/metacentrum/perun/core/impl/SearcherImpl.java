package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.implApi.SearcherImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Searcher Class for searching objects by Map of Attributes
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class SearcherImpl implements SearcherImplApi {

	private final static Logger log = LoggerFactory.getLogger(SearcherImpl.class);

	private static NamedParameterJdbcTemplate jdbc;
	private static JdbcPerunTemplate jdbcTemplate;

	public SearcherImpl(DataSource perunPool) {
		jdbc = new NamedParameterJdbcTemplate(perunPool);
		jdbcTemplate = new JdbcPerunTemplate(perunPool);
		jdbc.getJdbcTemplate().setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
		jdbcTemplate.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	@Override
	public List<User> getUsers(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues) throws InternalErrorException {
		StringBuilder query = new StringBuilder();
		query.append("select distinct " + UsersManagerImpl.userMappingSelectQuery + " from users ");

		MapSqlParameterSource parameters = new MapSqlParameterSource();

		insertWhereClausesAndQueryParametersFromAttributes(query, parameters, "user_attr_values", "user", "users", attributesWithSearchingValues, false);

		try {
			return jdbc.query(query.toString(), parameters, UsersManagerImpl.USER_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Member> getMembersByExpiration(PerunSession sess, String operator, LocalDate date, int days) throws InternalErrorException {

		// this would default to now
		if (date == null) date = LocalDate.now();
		date = date.plusDays(days);
		// create sql toDate()
		String compareDate = "TO_DATE('"+date+"','YYYY-MM-DD')";

		if (operator == null || operator.isEmpty()) operator = "=";

		if (!operator.equals("<") && !operator.equals("<=") && !operator.equals("=") && !operator.equals(">=") && !operator.equals(">"))
			throw new InternalErrorException("Operator '"+operator+"' is not allowed in SQL.");

		try {

			String query = "select distinct " + MembersManagerImpl.memberMappingSelectQuery + " from member_attr_values val" +
					" join members on val.member_id=members.id" +
					" and val.attr_id=(select id from attr_names where attr_name='urn:perun:member:attribute-def:def:membershipExpiration')" +
					" and TO_DATE(val.attr_value, 'YYYY-MM-DD')"+operator+compareDate;

			return jdbcTemplate.query(query, MembersManagerImpl.MEMBER_MAPPER);

		} catch (Exception e) {
			throw new InternalErrorException(e);
		}

	}

	@Override
	public List<Group> getGroupsByGroupResourceSetting(PerunSession sess, Attribute groupResourceAttribute, Attribute resourceAttribute) throws InternalErrorException {
		try {
			return jdbcTemplate.query("select  " + GroupsManagerImpl.groupMappingSelectQuery + " from groups where groups.id in ( " +
							"select distinct gr.group_id from groups_resources gr " +
							"join group_resource_attr_values grav on gr.group_id=grav.group_id and gr.resource_id=grav.resource_id " +
							"join resource_attr_values rav on gr.resource_id=rav.resource_id " +
							"where rav.attr_id=? and grav.attr_id=? and rav.attr_value=? and grav.attr_value=?)", GroupsManagerImpl.GROUP_MAPPER,
					resourceAttribute.getId(), groupResourceAttribute.getId(), resourceAttribute.getValue(), groupResourceAttribute.getValue());
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);

		}
	}

	@Override
	public List<Facility> getFacilities(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues) throws InternalErrorException {
		StringBuilder query = new StringBuilder();
		query.append("select distinct " + FacilitiesManagerImpl.facilityMappingSelectQuery + " from facilities ");

		MapSqlParameterSource parameters = new MapSqlParameterSource();

		insertWhereClausesAndQueryParametersFromAttributes(query, parameters, "facility_attr_values", "facility", "facilities", attributesWithSearchingValues, false);

		try {
			return jdbc.query(query.toString(), parameters, FacilitiesManagerImpl.FACILITY_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Resource> getResources(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues, boolean allowPartialMatchForString) throws InternalErrorException {
		StringBuilder query = new StringBuilder();
		query.append("select distinct " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resources ");

		MapSqlParameterSource parameters = new MapSqlParameterSource();

		insertWhereClausesAndQueryParametersFromAttributes(query, parameters, "resource_attr_values", "resource", "resources", attributesWithSearchingValues, allowPartialMatchForString);

		try {
			return jdbc.query(query.toString(), parameters, ResourcesManagerImpl.RESOURCE_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Generates into given query 'WHERE' clauses based on values from
	 * given Map. Into given parameters adds objects needed in generated clauses.
	 *
	 * @param query output where are the generated clauses appended
	 * @param parameters output where are added objects used in where clauses
	 * @param attributesWithSearchingValues attributes with values used for generating WHERE clauses
	 * @param allowPartialMatchForString if false, search only by exact match, if true, search also by partial match (for String values only!)
	 * @throws InternalErrorException internal error
	 */
	@SuppressWarnings("ConstantConditions")
	private void insertWhereClausesAndQueryParametersFromAttributes(StringBuilder query, MapSqlParameterSource parameters,
	                                                                String attrValueTableName, String entityName, String entityTableName,
	                                                                Map<Attribute, String> attributesWithSearchingValues, boolean allowPartialMatchForString) throws InternalErrorException {
		List<String> whereClauses = new ArrayList<>();
		int counter = 0;
		for(Attribute key: attributesWithSearchingValues.keySet()) {
			counter++;
			String value = attributesWithSearchingValues.get(key);
			query.append("left join ").append(attrValueTableName).append(" val").append(counter).append(" ");
			query.append("on val").append(counter).append(".").append(entityName).append("_id=").append(entityTableName).append(".id and val").append(counter).append(".attr_id=").append(key.getId()).append(" ");
			query.append("left join attr_names nam").append(counter).append(" on val").append(counter).append(".attr_id=nam").append(counter).append(".id ");

			if (value == null || value.isEmpty()) {
				if(key.getType().equals(LinkedHashMap.class.getName()) ||
						key.getType().equals(BeansUtils.largeStringClassName) ||
						key.getType().equals(BeansUtils.largeArrayListClassName)) {
					whereClauses.add("val" + counter + ".attr_value_text IS NULL ");
				} else {
					whereClauses.add("val" + counter + ".attr_value IS NULL ");
				}
			} else {
				if (key.getType().equals(Integer.class.getName())) {
					key.setValue(Integer.valueOf(value));
					whereClauses.add("val" + counter + ".attr_value=:v" + counter + " ");
					whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
					parameters.addValue("n" + counter, Integer.class.getName());
					parameters.addValue("v" + counter, BeansUtils.attributeValueToString(key));
				} else if (key.getType().equals(String.class.getName())) {
					key.setValue(value);
					if(allowPartialMatchForString) {
						whereClauses.add("lower(" + Compatibility.convertToAscii("val" + counter + ".attr_value") + ") LIKE CONCAT('%', CONCAT(lower(" + Compatibility.convertToAscii(":v" + counter) + "), '%')) ");
					} else {
						whereClauses.add("lower(" + Compatibility.convertToAscii("val" + counter + ".attr_value") + ")=lower(" + Compatibility.convertToAscii(":v" + counter) + ") ");
					}
					whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
					parameters.addValue("n" + counter, String.class.getName());
					parameters.addValue("v" + counter, BeansUtils.attributeValueToString(key));
				} else if (key.getType().equals(BeansUtils.largeStringClassName)) {
					key.setValue(value);
					if(allowPartialMatchForString) {
						whereClauses.add("lower(" + Compatibility.convertToAscii("val" + counter + ".attr_value_text") + ") LIKE CONCAT('%', CONCAT(lower(" + Compatibility.convertToAscii(":v" + counter) + "), '%')) ");
					} else {
						whereClauses.add("lower(" + Compatibility.convertToAscii("val" + counter + ".attr_value_text") + ")=lower(" + Compatibility.convertToAscii(":v" + counter) + ") ");
					}
					whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
					parameters.addValue("n" + counter, BeansUtils.largeStringClassName);
					parameters.addValue("v" + counter, BeansUtils.attributeValueToString(key));
				} else if (key.getType().equals(Boolean.class.getName())) {
					key.setValue(value);
					whereClauses.add("lower("+Compatibility.convertToAscii("val" + counter + ".attr_value")+")=lower("+Compatibility.convertToAscii(":v"+counter)+") ");
					whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
					parameters.addValue("n" + counter, Boolean.class.getName());
					parameters.addValue("v" + counter, BeansUtils.attributeValueToString(key));
				} else if (key.getType().equals(ArrayList.class.getName())) {
					List<String> list = new ArrayList<>();
					list.add(value);
					key.setValue(list);
					whereClauses.add("val" + counter + ".attr_value LIKE :v" + counter + " ");
					whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
					parameters.addValue("n" + counter, ArrayList.class.getName());
					// key can not be null because value is not null due to previous check
					parameters.addValue("v" + counter, '%' + BeansUtils.attributeValueToString(key).substring(0, BeansUtils.attributeValueToString(key).length() - 1) + '%');
				} else if (key.getType().equals(BeansUtils.largeArrayListClassName)) {
					List<String> list = new ArrayList<>();
					list.add(value);
					key.setValue(list);
					whereClauses.add("val" + counter + ".attr_value_text LIKE :v" + counter + " ");
					whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
					parameters.addValue("n" + counter, BeansUtils.largeArrayListClassName);
					// key can not be null because value is not null due to previous check
					parameters.addValue("v" + counter, '%' + BeansUtils.attributeValueToString(key).substring(0, BeansUtils.attributeValueToString(key).length() - 1) + '%');
				} else if (key.getType().equals(LinkedHashMap.class.getName())) {
					String[] splitMapItem = value.split("=");
					if(splitMapItem.length == 0) throw new InternalErrorException("Value can't be split by char '='.");
					String splitKey = splitMapItem[0];
					StringBuilder splitValue = new StringBuilder();
					if(splitMapItem.length > 1) {
						for(int i=1;i<splitMapItem.length;i++) {
							if(i!=1) splitValue.append('=');
							splitValue.append(splitMapItem[i]);
						}
					}
					Map<String, String> map = new LinkedHashMap<>();
					map.put(splitKey, splitValue.length() == 0 ? null : splitValue.toString());
					key.setValue(map);
					whereClauses.add("val" + counter + ".attr_value_text LIKE :v" + counter + " or val" + counter + ".attr_value_text LIKE :vv" + counter + " ");
					whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
					parameters.addValue("n" + counter, LinkedHashMap.class.getName());
					parameters.addValue("v" + counter, BeansUtils.attributeValueToString(key) + '%');
					parameters.addValue("vv" + counter,  "%," +  BeansUtils.attributeValueToString(key) + '%');
				} else {
					throw new InternalErrorException(key + " is not type of integer, string, boolean, array or hashmap.");
				}
			}
		}

		//Add Where clauses at end of sql query
		boolean first = true;
		for(String whereClause: whereClauses) {
			if(first) {
				query.append("where ");
				query.append(whereClause);
				first = false;
			} else {
				query.append("and ");
				query.append(whereClause);
			}
		}
	}

	@Override
	public List<Member> getMembersByGroupExpiration(PerunSession sess, Group group, String operator, LocalDate date, int days) throws InternalErrorException {

		// if date is null, use today
		if (date == null) {
			date = LocalDate.now();
		}
		date = date.plusDays(days);
		// create sql toDate()
		String compareDate = "TO_DATE('"+date+"','YYYY-MM-DD')";

		if (operator == null || operator.isEmpty()) {
			operator = "=";
		}

		if (!operator.equals("<") && !operator.equals("<=") && !operator.equals("=") && !operator.equals(">=") && !operator.equals(">")) {
			throw new InternalErrorException("Operator '"+operator+"' is not allowed in SQL.");
		}

		try {

			String query = "select distinct " + MembersManagerImpl.groupsMembersMappingSelectQuery + " from member_group_attr_values val" +
					" join groups_members on val.member_id=groups_members.member_id" +
					" and val.group_id=groups_members.group_id" +
					" and val.group_id=?" +
					" and val.attr_id=(select id from attr_names where attr_name='urn:perun:member_group:attribute-def:def:groupMembershipExpiration')" +
					" and TO_DATE(val.attr_value, 'YYYY-MM-DD')" + operator + compareDate +
					" join members on members.id=val.member_id";

			return jdbcTemplate.query(query, MembersManagerImpl.MEMBERS_WITH_GROUP_STATUSES_SET_EXTRACTOR, group.getId());

		} catch (Exception e) {
			throw new InternalErrorException(e);
		}
	}
}
