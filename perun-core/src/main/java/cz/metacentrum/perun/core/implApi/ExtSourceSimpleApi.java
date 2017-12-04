package cz.metacentrum.perun.core.implApi;

import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;

/**
 * Definition of simple extSource api.
 *
 * Simple means: this extSource is not able to get all information about all subjects
 * in one query. First need to get info about their logins and then for every login get
 * info about subject from extSource.
 *
 * @author Michal Prochazka michalp@ics.muni.cz (extSourceApi)
 * @author Michal Stava stavamichal@gmail.com (changed to ExtSourceSimpleApi)
 */
public interface ExtSourceSimpleApi {
	/**
	 * Finds all subjects logins in the external source, that contains searchString.
	 *
	 * This method is used for getting all logins of subjects in external source and then use them to searching
	 * in external source for other subjects attributes.
	 *
	 * @param searchString
	 * @return list of maps, which contains attr_name-&gt;attr_value but only for login definition eg. login;MichalS
	 * @throws InternalErrorException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	List<Map<String, String>> findSubjectsLogins(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException;

	/**
	 * Finds all subjects logins in the external source, that contains searchString, limited by the maxResults.
	 *
	 * This method is used for getting all logins of subjects in external source and then use them to searching
	 * in external source for other subjects attributes
	 *
	 * @param searchString
	 * @param maxResults limit returned results
	 * @return list of maps, which contains attr_name-&gt;attr_value but only for login definition eg. login;MichalS
	 * @throws InternalErrorException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException;

	/**
	 * Finds subject from the external source by the primary login used in external source.
	 *
	 * @param login login used in the external source
	 * @return map which contains attr_name -&gt; attr_value, e.g. firstName-&gt;Michal
	 * @throws InternalErrorException
	 * @throws SubjectNotExistsException if the subject cannot be found
	 * @throws ExtSourceUnsupportedOperationException
	 */
	Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException, ExtSourceUnsupportedOperationException;

	/**
	 * Get the list of the subjects in the external group.
	 *
	 * @param attributes map of attributes used for quering the external source
	 * @return list of maps, which contains attr_name-&gt;attr_value, e.g. firstName-&gt;Michal
	 * @throws InternalErrorException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException, ExtSourceUnsupportedOperationException;

	/**
	 * If extSource needs to be closed, this method must be called.
	 *
	 * @throws InternalErrorException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	void close() throws InternalErrorException, ExtSourceUnsupportedOperationException;

	/**
	 * Get the list of the subject groups in the external source.
	 *
	 * 	 * 1. Get the query by which will be subject groups selected
	 * 	 * 2. Get the list of subjects
	 *
	 * @param attributes map of attributes used for quering the external source
	 * @return list of maps, which contains attr name and attr value
	 * @throws InternalErrorException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	List<Map<String, String>> getSubjectGroups(Map<String, String> attributes) throws InternalErrorException, ExtSourceUnsupportedOperationException;

}
