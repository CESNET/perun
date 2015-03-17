package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.util.List;
import java.util.Map;

/**
 * Definition of extSource api.
 *
 * This extSource can get information about all subjects with all attributes by
 * one query.
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public interface ExtSourceApi extends ExtSourceSimpleApi {

	/**
	 * Finds all subjects with attributes in the external source, that contains searchString.
	 *
	 * This method is used for getting all logins of subjects in external source with all possible attributes.
	 *
	 * @param searchString
	 * @return list of maps, which contains attr_name-&gt;attr_value (for all extSource attributes)
	 * @throws InternalErrorException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	List<Map<String, String>> findSubjects(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException;

	/**
	 * Finds all subjects with attributes in the external source, that contains searchString limited by the maxResults
	 *
	 * This method is used for getting all logins of subjects in external source with all possible attributes.
	 *
	 * @param searchString
	 * @param maxResults define max number of returned results, 0 means unlimited
	 * @return list of maps, which contains attr_name-&gt;attr_value (for all extSource attributes)
	 * @throws InternalErrorException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException;
}
