package cz.metacentrum.perun.core.implApi;

import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;

/**
 * @author Michal Prochazka michalp@ics.muni.cz
 */
public interface ExtSourceApi {
	/**
	 * Finds all subjects in the external source, that contains searchString.
	 *
	 * @param searchString
	 * @return list of maps, which contains attr_name-&gt;attr_value, e.g. firstName-&gt;Michal
	 * @throws InternalErrorException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	List<Map<String, String>> findSubjects(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException;

	/**
	 * Finds all subjects in the external source, that contains searchString, limited by the maxResults
	 *
	 * @param searchString
	 * @param maxResults limit returned results
	 * @return list of maps, which contains attr_name-&gt;attr_value, e.g. firstName-&gt;Michal
	 * @throws InternalErrorException
	 * @throws ExtSourceUnsupportedOperationException
	 */
	List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException;

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
}
