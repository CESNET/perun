package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a specific version of SQL extSource extension for SQL Complex.
 * There is a new parameter which prepare complex sql query to be used by filling two parameters instead of whole
 * sql query (to prevent duplicity of the same sql query for every affected group
 */
public class ExtSourceINET extends ExtSourceSqlComplex {

	Pattern queryParametersPattern = Pattern.compile("^([1-9][0-9]+)[:]([-A-Za-z0-9]+)$");

	@Override
	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException {
		// Parameters of query in the combination "workspace:groupname"
		String sqlParametersForQuery = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);
		Matcher sqlParametersMatcher = queryParametersPattern.matcher(sqlParametersForQuery);
		if(!sqlParametersMatcher.matches()) throw new InternalErrorException("Parameters of group members query are not in expected format 'workspace:groupname'.");
		String numberOfWorkplace = sqlParametersMatcher.group(1);
		String nameOfTheGroup = sqlParametersMatcher.group(2);

		String queryTemplate = getAttributes().get("queryTemplate");
		if(!queryTemplate.contains("?")) throw new InternalErrorException("There is missing first occurence of '?' character to replace workplace number for!");
		String query = queryTemplate.replaceFirst("[?]", numberOfWorkplace);
		if(!query.contains("?")) throw new InternalErrorException("There is missing second occurence of '?' character to replace group name for!");
		query = query.replaceFirst("[?]", "'" + nameOfTheGroup + "'");

		return this.querySource(query, null, 0);
	}
}
