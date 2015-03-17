package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.implApi.SearcherImplApi;

import java.util.*;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.bl.PerunBl;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Searcher Class for searching objects by Map of Attributes
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class SearcherImpl implements SearcherImplApi {

	private final static Logger log = LoggerFactory.getLogger(SearcherImpl.class);

	private static NamedParameterJdbcTemplate jdbc;
	private static JdbcTemplate jdbcTemplate;

	public SearcherImpl(DataSource perunPool) {
		jdbc = new NamedParameterJdbcTemplate(perunPool);
		jdbcTemplate = new JdbcTemplate(perunPool);
	}

	public List<User> getUsers(PerunSession sess, Map<Attribute, String> attributesWithSearchingValues) throws InternalErrorException {
		StringBuilder query = new StringBuilder();
		query.append("select distinct " + UsersManagerImpl.userMappingSelectQuery + " from users ");

		List<String> whereClauses = new ArrayList<String>();
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		int counter = 0;

		for(Attribute key: attributesWithSearchingValues.keySet()) {
			counter++;
			String value = attributesWithSearchingValues.get(key);
			query.append("left join user_attr_values val" + counter + " ");
			query.append("on val" + counter + ".user_id=users.id and val" + counter + ".attr_id=" + key.getId() + " ");
			query.append("left join attr_names nam" + counter + " on val" + counter + ".attr_id=nam" + counter + ".id ");
			if (value == null) {
				if(key.getType().equals(LinkedHashMap.class.getName())) {
					whereClauses.add("val" + counter + ".attr_value_text IS NULL ");
				} else {
					whereClauses.add("val" + counter + ".attr_value IS NULL ");
				}
			} else if(value.isEmpty()) {
				if(key.getType().equals(LinkedHashMap.class.getName())) {
					whereClauses.add("val" + counter + ".attr_value_text IS NOT NULL ");
				} else {
					whereClauses.add("val" + counter + ".attr_value IS NOT NULL ");
				}
			} else {
				if (key.getType().equals(Integer.class.getName())) {
					key.setValue(Integer.valueOf(value));
					whereClauses.add("val" + counter + ".attr_value=:v" + counter + " ");
					whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
					parameters.addValue("n" + counter, Integer.class.getName().toString());
					parameters.addValue("v" + counter, BeansUtils.attributeValueToString(key));
				} else if (key.getType().equals(String.class.getName())) {
					key.setValue(value);
					whereClauses.add("lower("+Compatibility.convertToAscii("val" + counter + ".attr_value")+")=lower("+Compatibility.convertToAscii(":v"+counter)+") ");
					whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
					parameters.addValue("n" + counter, String.class.getName().toString());
					parameters.addValue("v" + counter, BeansUtils.attributeValueToString(key));
				} else if (key.getType().equals(Boolean.class.getName())) {
					key.setValue(value);
					whereClauses.add("lower("+Compatibility.convertToAscii("val" + counter + ".attr_value")+")=lower("+Compatibility.convertToAscii(":v"+counter)+") ");
					whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
					parameters.addValue("n" + counter, Boolean.class.getName().toString());
					parameters.addValue("v" + counter, BeansUtils.attributeValueToString(key));
				} else if (key.getType().equals(ArrayList.class.getName())) {
					List<String> list = new ArrayList<String>();
					list.add(value);
					key.setValue(list);
					whereClauses.add("val" + counter + ".attr_value LIKE :v" + counter + " ");
					whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
					parameters.addValue("n" + counter, ArrayList.class.getName().toString());
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
					Map<String, String> map = new LinkedHashMap<String, String>();
					map.put(splitKey, splitValue.length() == 0 ? null : splitValue.toString());
					key.setValue(map);
					whereClauses.add("val" + counter + ".attr_value_text LIKE :v" + counter + " or val" + counter + ".attr_value_text LIKE :vv" + counter + " ");
					whereClauses.add("nam" + counter + ".type=:n" + counter + " ");
					parameters.addValue("n" + counter, LinkedHashMap.class.getName().toString());
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

		try {
			if (Compatibility.isOracle()) {
				return jdbc.query(query.toString(), parameters, UsersManagerImpl.USER_MAPPER);
			} else if (Compatibility.isPostgreSql()) {
				return jdbc.query(query.toString(), parameters, UsersManagerImpl.USER_MAPPER);
				//throw new InternalErrorException("Unsupported postgreSQL type");
			} else {
				throw new InternalErrorException("Unsupported db type");
			}

		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<User>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Member> getMembersByExpiration(PerunSession sess, String operator, Calendar date, int days) throws InternalErrorException {

		// this would default to now
		if (date == null) date = Calendar.getInstance();
		date.add(Calendar.DAY_OF_MONTH, days);
		// create sql toDate()
		String compareDate = BeansUtils.getDateFormatterWithoutTime().format(date.getTime());
		compareDate = "TO_DATE('"+compareDate+"','yyyy-MM-dd')";

		if (operator == null || operator.isEmpty()) operator = "=";

		if (!operator.equals("<") && !operator.equals("<=") && !operator.equals("=") && !operator.equals(">=") && !operator.equals(">"))
			throw new InternalErrorException("Operator '"+operator+"' is not allowed in SQL.");

		try {

			AttributeDefinition def = ((PerunBl) sess.getPerun()).getAttributesManagerBl().getAttributeDefinition(sess, "urn:perun:member:attribute-def:def:membershipExpiration");

			String query = "select distinct " + MembersManagerImpl.memberMappingSelectQuery + " from members left join member_attr_values val on " +
					"val.member_id=members.id and val.attr_id=? where TO_DATE(val.attr_value, 'yyyy-MM-dd')"+operator+compareDate;

			return jdbcTemplate.query(query.toString(), MembersManagerImpl.MEMBER_MAPPER, def.getId());

		} catch (Exception e) {
			throw new InternalErrorException(e);
		}

	}

}
