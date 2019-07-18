package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ExtSource class for REMS, filters subjects that does not have a corresponding user in Perun
 * by ues REMS or by additionalueses in format: {extSourceName}|{extSourceClass}|{eppn}|0.
 * The eppn is used as a 'login'.
 * @author Vojtech Sassmann &lt;vojtech.sassmann@gmail.com&gt;
 */
public class ExtSourceREMS extends ExtSourceSqlComplex implements ExtSourceApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourceREMS.class);

	private static PerunBlImpl perunBl;

	// filled by spring (perun-core.xml)
	public static PerunBlImpl setPerunBlImpl(PerunBlImpl perun) {
		perunBl = perun;
		return perun;
	}

	@Override
	public List<Map<String, String>> findSubjects(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		List<Map<String, String>> subjects = super.findSubjects(searchString);
		return filterNonExistingUsers(subjects);
	}

	@Override
	public List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		List<Map<String, String>> subjects = super.findSubjects(searchString, maxResults);
		return filterNonExistingUsers(subjects);
	}

	@Override
	public Map<String, String> getSubjectByLogin(String login) throws InternalErrorException, SubjectNotExistsException {
		Map<String, String> subject = super.getSubjectByLogin(login);
		if (!isExistingUser(subject)) {
			throw new SubjectNotExistsException("Subject for given login does not exist in Perun");
		}
		return subject;
	}

	@Override
	public List<Map<String, String>> findSubjectsLogins(String searchString) throws InternalErrorException {
		List<Map<String, String>> subjects = super.findSubjectsLogins(searchString);
		return filterNonExistingUsers(subjects);
	}

	@Override
	public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) throws InternalErrorException {
		List<Map<String, String>> subjects = super.findSubjectsLogins(searchString, maxResults);
		return filterNonExistingUsers(subjects);
	}

	@Override
	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException {
		List<Map<String, String>> subjects = super.getGroupSubjects(attributes);
		return filterNonExistingUsers(subjects);
	}

	@Override
	public List<Map<String, String>> getSubjectGroups(Map<String, String> attributes) throws ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	public List<Map<String, String>> getUsersSubjects() throws ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	/**
	 * Filters subjects that does not have a corresponding user in Perun by ues REMS
	 * or by additionalueses in format: {extSourceName}|{extSourceClass}|{eppn}|0.
	 * The eppn is used as a 'login'.
	 *
	 * @param subjects subjects that will be filtered
	 * @return List without filtered subjects
	 * @throws InternalErrorException internalError
	 */
	private List<Map<String, String>> filterNonExistingUsers(List<Map<String, String>> subjects) throws InternalErrorException {
		List<Map<String, String>> existingSubjects = new ArrayList<>();

		for (Map<String, String> subject : subjects) {
			if(isExistingUser(subject)) {
				existingSubjects.add(subject);
			}
		}

		return existingSubjects;
	}

	/**
	 * Checks if the given subject has a corresponding user in Perun by ues REMS
	 * or by additionalueses in format: {extSourceName}|{extSourceClass}|{eppn}|0.
	 * The eppn is used as a 'login'.
	 *
	 * @param subject subject
	 * @return true if the subject has, false otherwise
	 * @throws InternalErrorException internalError
	 */
	private boolean isExistingUser(Map<String, String> subject) throws InternalErrorException {
		if (subject == null || subject.isEmpty()) {
			throw new InternalErrorException("Subject can not be empty or null: " + subject);
		}

		String login = subject.get("login");

		if (login == null || login.isEmpty()) {
			log.error("Failed to get user's login from subject {}");
			return false;
		}

		PerunSession sess = getSession();

		// test if subject does not exist already with UserExtSourceREMS. If so, it means that subject already exists in perun.
		List<User> usersFromREMS = perunBl.getUsersManagerBl().getUsersByExtSourceTypeAndLogin(sess, ExtSourcesManager.EXTSOURCE_REMS, login);
		if (usersFromREMS.size() > 0) {
			return true;
		}

		List<String> extSources = getAdditionalUESes(subject);


		if (extSources.isEmpty()) {
			log.error("Failed to get any additionalues from subject {}", subject);
			return false;
		}

		for (String ues : extSources) {
			if (existsSubjectWithUes(ues)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Finds out for given ues and login exits user in Perun.
	 * Format of ues is {extSourceName}|{extSourceClass}|{eppn}|0.
	 * The eppn is used as a 'login'.
	 *
	 * @param ues ues with user login: {extSourceName}|{extSourceClass}|{eppn}|0
	 * @return true if is found existing ues with given login, false otherwise
	 * @throws InternalErrorException internalError
	 */
	private boolean existsSubjectWithUes(String ues) throws InternalErrorException {
		String[] extSourceSplit = ues.split("\\|", 4);
		if(extSourceSplit.length != 4) {
			log.error("Ivalid format of additionalues_1. It should be '{extSourceName}|{extSourceClass}|{eppn}|0'. Actual: {}", ues);
			return false;
		}

		PerunSession sess = getSession();

		String extSourceName = extSourceSplit[0];
		String eppn = extSourceSplit[2];

		try {
			// try to find user by additionalues
			perunBl.getUsersManagerBl().getUserByExtSourceNameAndExtLogin(sess, extSourceName, eppn);
			return true;
		} catch (ExtSourceNotExistsException | UserExtSourceNotExistsException e) {
			log.error("Failed to get extSource with name '{}'", extSourceName);
		} catch (UserNotExistsException e) {
			return false;
		}
		return false;
	}

	/**
	 * Returns a List of Strings with additional UESes.
	 *
	 * @param subject subject
	 * @return List of additional UESes
	 */
	private List<String> getAdditionalUESes(Map<String, String> subject) {
		List<String> extSources = new ArrayList<>();

		Set<String> keys = subject.keySet();

		for (String key : keys) {
			if (key.startsWith("additionalues_")) {
				extSources.add(subject.get(key));
			}
		}

		return extSources;
	}

	private PerunSession getSession() throws InternalErrorException {
		final PerunPrincipal pp = new PerunPrincipal("ExtSourceREMS", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		try {
			return perunBl.getPerunSession(pp, new PerunClient());
		} catch (InternalErrorException e) {
			throw new InternalErrorException("Failed to get session for ExtSourceREMS.", e);
		}
	}
}
