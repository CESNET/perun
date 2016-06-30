/**
 *
 */
package cz.metacentrum.perun.core.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;

/**
 * Dummy ExtSource - X.508
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 */
public class ExtSourceX509 extends ExtSource implements ExtSourceSimpleApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourceX509.class);

	public List<Map<String,String>> findSubjectsLogins(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		return findSubjectsLogins(searchString, 0);
	}

	public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	public Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes, List<String> logins) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException("Not supported to get subjects for this extSource by list of logins.");
	}

	public void close() throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}
}
