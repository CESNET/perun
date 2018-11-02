package cz.metacentrum.perun.core.blImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceAddedToGroup;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceAddedToVo;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceCreated;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceDeleted;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceRemovedFromGroup;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceRemovedFromVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ParserException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;

import cz.metacentrum.perun.core.bl.ExtSourcesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.ExtSourcesManagerImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import cz.metacentrum.perun.core.implApi.ExtSourcesManagerImplApi;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtSourcesManagerBlImpl implements ExtSourcesManagerBl {
	final static Logger log = LoggerFactory.getLogger(ExtSourcesManagerBlImpl.class);
	// \\p{L} means any unicode char
	private static final Pattern namePattern = Pattern.compile("^[\\p{L} ,.0-9_'-]+$");

	private final ExtSourcesManagerImplApi extSourcesManagerImpl;
	private PerunBl perunBl;
	private AtomicBoolean initialized = new AtomicBoolean(false);


	public ExtSourcesManagerBlImpl(ExtSourcesManagerImplApi extSourcesManagerImpl) {
		this.extSourcesManagerImpl = extSourcesManagerImpl;
	}

	@Override
	public void initialize(PerunSession sess) {
		if (!this.initialized.compareAndSet(false, true)) return;
		this.extSourcesManagerImpl.initialize(sess);
	}

	@Override
	public ExtSource createExtSource(PerunSession sess, ExtSource extSource, Map<String, String> attributes) throws InternalErrorException, ExtSourceExistsException {
		getPerunBl().getAuditer().log(sess, new ExtSourceCreated(extSource));
		return getExtSourcesManagerImpl().createExtSource(sess, extSource, attributes);
	}

	@Override
	public void deleteExtSource(PerunSession sess, ExtSource extSource) throws InternalErrorException, ExtSourceAlreadyRemovedException {
		getExtSourcesManagerImpl().deleteExtSource(sess, extSource);
		getPerunBl().getAuditer().log(sess, new ExtSourceDeleted(extSource));
	}

	@Override
	public ExtSource getExtSourceById(PerunSession sess, int id) throws InternalErrorException, ExtSourceNotExistsException {
		return getExtSourcesManagerImpl().getExtSourceById(sess, id);
	}

	@Override
	public ExtSource getExtSourceByName(PerunSession sess, String name) throws InternalErrorException, ExtSourceNotExistsException {
		return getExtSourcesManagerImpl().getExtSourceByName(sess, name);
	}

	@Override
	public List<ExtSource> getVoExtSources(PerunSession sess, Vo vo) throws InternalErrorException {
		return getExtSourcesManagerImpl().getVoExtSources(sess, vo);
	}

	@Override
	public List<ExtSource> getGroupExtSources(PerunSession sess, Group group) throws InternalErrorException {
		return getExtSourcesManagerImpl().getGroupExtSources(sess, group);
	}

	@Override
	public List<ExtSource> getExtSources(PerunSession sess) throws InternalErrorException {
		return getExtSourcesManagerImpl().getExtSources(sess);
	}
	@Override
	public void addExtSource(PerunSession sess, Vo vo, ExtSource source) throws InternalErrorException, ExtSourceAlreadyAssignedException {
		getExtSourcesManagerImpl().addExtSource(sess, vo, source);
		getPerunBl().getAuditer().log(sess, new ExtSourceAddedToVo(source, vo));
	}

	@Override
	public void addExtSource(PerunSession sess, Group group, ExtSource source) throws InternalErrorException, ExtSourceAlreadyAssignedException {
		getExtSourcesManagerImpl().addExtSource(sess, group, source);
		getPerunBl().getAuditer().log(sess, new ExtSourceAddedToGroup(source, group));
	}

	@Override
	public ExtSource checkOrCreateExtSource(PerunSession sess, String extSourceName, String extSourceType) throws InternalErrorException {
		// Check if the extSource exists
		try {
			return getExtSourcesManagerImpl().getExtSourceByName(sess, extSourceName);
		} catch (ExtSourceNotExistsException e) {
			// extSource doesn't exist, so create new one
			ExtSource extSource = new ExtSource();
			extSource.setName(extSourceName);
			extSource.setType(extSourceType);
			try {
				return this.createExtSource(sess, extSource, null);
			} catch (ExtSourceExistsException e1) {
				throw new ConsistencyErrorException("Creating existing extSource", e1);
			}
		}
	}

	@Override
	public void removeExtSource(PerunSession sess, Vo vo, ExtSource source) throws InternalErrorException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		List<Group> groupsWithAssignedExtSource = getPerunBl().getGroupsManagerBl().getGroupsWithAssignedExtSourceInVo(sess, source, vo);
		for(Group group: groupsWithAssignedExtSource) {
			getPerunBl().getExtSourcesManagerBl().removeExtSource(sess, group, source);
		}

		getExtSourcesManagerImpl().removeExtSource(sess, vo, source);
		getPerunBl().getAuditer().log(sess,new ExtSourceRemovedFromVo(source, vo));
	}

	@Override
	public void removeExtSource(PerunSession sess, Group group, ExtSource source) throws InternalErrorException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		getExtSourcesManagerImpl().removeExtSource(sess, group, source);
		getPerunBl().getAuditer().log(sess,new ExtSourceRemovedFromGroup(source, group));
	}

	@Override
	public List<User> getInvalidUsers(PerunSession sess, ExtSource source) throws InternalErrorException {
		List<Integer> usersIds;
		List<User> invalidUsers = new ArrayList<User>();

		// Get all users, who are associated with this extSource
		usersIds = getExtSourcesManagerImpl().getAssociatedUsersIdsWithExtSource(sess, source);
		List<User> users = getPerunBl().getUsersManagerBl().getUsersByIds(sess, usersIds);

		for (User user: users) {
			// From user's userExtSources get the login
			String userLogin = "";

			List<UserExtSource> userExtSources = getPerunBl().getUsersManagerBl().getUserExtSources(sess, user);
			for (UserExtSource userExtSource: userExtSources) {
				if (userExtSource.getExtSource().equals(source)) {
					// It is enough to have at least one login from the extSource
					// TODO jak budeme kontrolovat, ze mu zmizel jeden login a zustal jiny, zajima nas to?
					userLogin = userExtSource.getLogin();
				}
			}

			// Check if the login is still present in the extSource
			try {
				((ExtSourceSimpleApi) source).getSubjectByLogin(userLogin);
			} catch (SubjectNotExistsException e) {
				invalidUsers.add(user);
			} catch (ExtSourceUnsupportedOperationException e) {
				log.warn("ExtSource {} doesn't support getSubjectByLogin", source.getName());
				continue;
			}
		}

		return invalidUsers;
	}

	/**
	 * Gets the extSourcesManagerImpl for this instance.
	 *
	 * @return extSourceManagerImpl
	 */
	public ExtSourcesManagerImplApi getExtSourcesManagerImpl() {
		return this.extSourcesManagerImpl;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	@Override
	public void checkExtSourceExists(PerunSession sess, ExtSource extSource) throws InternalErrorException, ExtSourceNotExistsException {
		getExtSourcesManagerImpl().checkExtSourceExists(sess, extSource);
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	@Override
	public Candidate getCandidate(PerunSession sess, ExtSource source, String login) throws InternalErrorException, ExtSourceNotExistsException, CandidateNotExistsException, ExtSourceUnsupportedOperationException {
		// New Candidate
		Candidate candidate = new Candidate();

		// Prepare userExtSource object
		UserExtSource userExtSource = new UserExtSource();
		userExtSource.setExtSource(source);
		userExtSource.setLogin(login);

		// Set the userExtSource
		candidate.setUserExtSource(userExtSource);

		// Get the subject from the extSource
		Map<String, String> subject;
		try {
			subject = ((ExtSourceSimpleApi) source).getSubjectByLogin(login);
		} catch (SubjectNotExistsException e) {
			throw new CandidateNotExistsException(login);
		}

		if (subject == null) {
			throw new CandidateNotExistsException("Candidate with login [" + login + "] not exists");
		}

		// Count hash code of subject
		int hashCode = subject.hashCode();
		subject.put(MembersManager.MEMBERGROUPHASHCODE_ATTRNAME, Integer.toString(hashCode));

		// Set candidate from subject
		setCandidateFromSubject(candidate, subject);

		// Additional userExtSources
		List<UserExtSource> additionalUserExtSources = new ArrayList<UserExtSource>();
		// Attributes of candidate
		Map<String, String> attributes = new HashMap<String, String>();
		// Filter attributes stored in subject
		filterAttributes(sess, subject, login, attributes, additionalUserExtSources);

		// Set userExtSources and attributes to candidate
		candidate.setAdditionalUserExtSources(additionalUserExtSources);
		candidate.setAttributes(attributes);

		return candidate;
	}

	public Candidate getCandidate(PerunSession perunSession, Map<String,String> subject, ExtSource source, String login) throws InternalErrorException, ExtSourceNotExistsException, CandidateNotExistsException, ExtSourceUnsupportedOperationException {
		if(login == null || login.isEmpty()) throw new InternalErrorException("Login can't be empty or null.");
		if(subject == null || subject.isEmpty()) throw new InternalErrorException("Subject can't be null or empty, at least login there must exists.");

		// New Candidate
		Candidate candidate = new Candidate();

		// Prepare userExtSource object
		UserExtSource userExtSource = new UserExtSource();
		userExtSource.setExtSource(source);
		userExtSource.setLogin(login);

		// Count hash code of subject
		int hashCode = subject.hashCode();
		subject.put(MembersManager.MEMBERGROUPHASHCODE_ATTRNAME, Integer.toString(hashCode));

		// Set the userExtSource
		candidate.setUserExtSource(userExtSource);

		// Set candidate from subject
		setCandidateFromSubject(candidate, subject);

		// Additional userExtSources
		List<UserExtSource> additionalUserExtSources = new ArrayList<UserExtSource>();
		// Attributes of candidate
		Map<String, String> attributes = new HashMap<String, String>();
		// Filter attributes stored in subject
		filterAttributes(perunSession, subject, login, attributes, additionalUserExtSources);

		// Set userExtSources and attributes to candidate
		candidate.setAdditionalUserExtSources(additionalUserExtSources);
		candidate.setAttributes(attributes);

		return candidate;
	}

	/**
	 * Sets attributes and additional user external sources of candidate
	 *
	 * @param sess Perun session
	 * @param subject Subject to gain attributes from
	 * @param login Login of candidate
	 * @param attributes Attributes of candidate, which will be filled
	 * @param additionalUserExtSources Additional user external sources of candidate, which will be filled
	 * @throws InternalErrorException
	 */
	private void filterAttributes(PerunSession sess, Map<String, String> subject, String login,
												 Map<String, String> attributes, List<UserExtSource> additionalUserExtSources) throws InternalErrorException {
		for (String attrName: subject.keySet()) {
			// Allow only users and members attributes and hashCode of subject
			if (attrName.startsWith(AttributesManager.NS_MEMBER_ATTR)
					|| attrName.startsWith(AttributesManager.NS_USER_ATTR)
					|| attrName.equals(MembersManager.MEMBERGROUPHASHCODE_ATTRNAME)) {
				attributes.put(attrName, subject.get(attrName));
			} else if (attrName.startsWith(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING)) {
				if(subject.get(attrName) == null) continue; //skip null additional ext sources
				// Add additionalUserExtSources
				String[] userExtSourceRaw =  subject.get(attrName).split("\\|"); // Entry contains extSourceName|extSourceType|extLogin[|LoA]
				log.debug("Processing additionalUserExtSource {}",  subject.get(attrName));

				// Check if the array has at least 3 parts, this is protection against outOfBoundException
				if(userExtSourceRaw.length < 3) {
					throw new InternalErrorException("There is missing some mandatory part of additional user extSource value when processing it - '" + attrName + "'");
				}

				String additionalExtSourceName = userExtSourceRaw[0];
				String additionalExtSourceType = userExtSourceRaw[1];
				String additionalExtLogin = userExtSourceRaw[2];
				int additionalExtLoa = 0;
				// Loa is not mandatory argument
				if (userExtSourceRaw.length>3 && userExtSourceRaw[3] != null) {
					try {
						additionalExtLoa = Integer.parseInt(userExtSourceRaw[3]);
					} catch (NumberFormatException e) {
						throw new ParserException("Candidate with login [" + login + "] has wrong LoA '" + userExtSourceRaw[3] + "'.", e, "LoA");
					}
				}

				ExtSource additionalExtSource;

				if (additionalExtSourceName == null || additionalExtSourceName.isEmpty() ||
						additionalExtSourceType == null || additionalExtSourceType.isEmpty() ||
						additionalExtLogin == null || additionalExtLogin.isEmpty()) {
					log.error("User with login {} has invalid additional userExtSource defined {}.", login, userExtSourceRaw);
				} else {
					try {
						// Try to get extSource, with full extSource object (containg ID)
						additionalExtSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(sess, additionalExtSourceName);
					} catch (ExtSourceNotExistsException e) {
						try {
							// Create new one if not exists
							additionalExtSource = new ExtSource(additionalExtSourceName, additionalExtSourceType);
							additionalExtSource = getPerunBl().getExtSourcesManagerBl().createExtSource(sess, additionalExtSource, null);
						} catch (ExtSourceExistsException e1) {
							throw new ConsistencyErrorException("Creating existin extSource: " + additionalExtSourceName);
						}
					}
					// Add additional user extSource
					additionalUserExtSources.add(new UserExtSource(additionalExtSource, additionalExtLoa, additionalExtLogin));
				}
			}
		}
	}

	/**
	 * Sets main information about candidate from data in subject
	 * Sets: firstName, lastName, middleName, titleBefore, titleAfter, isServiceUser, isSponsoredUser
	 *
	 * @param candidate Candidate to be set
	 * @param subject Subject to gain values from
	 */
	private void setCandidateFromSubject(Candidate candidate, Map<String, String> subject) {
		// If first name of candidate is not in format of name, set null instead
		candidate.setFirstName(subject.get("firstName"));
		if(candidate.getFirstName() != null) {
			Matcher name = namePattern.matcher(candidate.getFirstName());
			if(!name.matches()) candidate.setFirstName(null);
		}
		// If last name of candidate is not in format of name, set null instead
		candidate.setLastName(subject.get("lastName"));
		if(candidate.getLastName()!= null) {
			Matcher name = namePattern.matcher(candidate.getLastName());
			if(!name.matches()) candidate.setLastName(null);
		}
		candidate.setMiddleName(subject.get("middleName"));
		candidate.setTitleAfter(subject.get("titleAfter"));
		candidate.setTitleBefore(subject.get("titleBefore"));

		// Set service user
		if(subject.get("isServiceUser") == null) {
			candidate.setServiceUser(false);
		} else {
			String isServiceUser = subject.get("isServiceUser");
			if(isServiceUser.equals("true")) {
				candidate.setServiceUser(true);
			} else {
				candidate.setServiceUser(false);
			}
		}

		// Set sponsored user
		if(subject.get("isSponsoredUser") == null) {
			candidate.setSponsoredUser(false);
		} else {
			String isSponsoredUser = subject.get("isSponsoredUser");
			if(isSponsoredUser.equals("true")) {
				candidate.setSponsoredUser(true);
			} else {
				candidate.setSponsoredUser(false);
			}
		}
	}

	@Override
	public void checkExtSourceAssignedToVo(PerunSession sess, ExtSource extSource, int voId) throws InternalErrorException, ExtSourceNotAssignedException, VoNotExistsException {
		Vo vo = getPerunBl().getVosManagerBl().getVoById(sess, voId);
		List<ExtSource> voExtSources = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);

		if(!voExtSources.contains(extSource)) throw new ExtSourceNotAssignedException("ExtSource " + extSource + " is not assigned to vo " + vo);
	}

	@Override
	public void loadExtSourcesDefinitions(PerunSession sess) {
		getExtSourcesManagerImpl().loadExtSourcesDefinitions(sess);
	}

	@Override
	public Map<String, String> getAttributes(ExtSource extSource) throws InternalErrorException {
		return getExtSourcesManagerImpl().getAttributes(extSource);
	}
}
