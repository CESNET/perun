package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceAddedToGroup;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceAddedToVo;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceCreated;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceDeleted;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceRemovedFromGroup;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceRemovedFromVo;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.CandidateGroup;
import cz.metacentrum.perun.core.api.CandidateSync;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichUserExtSource;
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
import cz.metacentrum.perun.core.api.exceptions.InvalidGroupNameException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;

import cz.metacentrum.perun.core.bl.ExtSourcesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import cz.metacentrum.perun.core.implApi.ExtSourcesManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cz.metacentrum.perun.core.blImpl.GroupsManagerBlImpl.GROUP_SYNC_DEFAULT_DATA;

public class ExtSourcesManagerBlImpl implements ExtSourcesManagerBl {
	final static Logger log = LoggerFactory.getLogger(ExtSourcesManagerBlImpl.class);
	// \\p{L} means any unicode char
	private static final Pattern namePattern = Pattern.compile("^[\\p{L} ,.0-9_'-]+$");

	private final ExtSourcesManagerImplApi extSourcesManagerImpl;
	private PerunBl perunBl;
	private final AtomicBoolean initialized = new AtomicBoolean(false);


	public ExtSourcesManagerBlImpl(ExtSourcesManagerImplApi extSourcesManagerImpl) {
		this.extSourcesManagerImpl = extSourcesManagerImpl;
	}

	@Override
	public void initialize(PerunSession sess) {
		if (!this.initialized.compareAndSet(false, true)) return;
		this.extSourcesManagerImpl.initialize(sess);
	}

	@Override
	public ExtSource createExtSource(PerunSession sess, ExtSource extSource, Map<String, String> attributes) throws ExtSourceExistsException {
		getPerunBl().getAuditer().log(sess, new ExtSourceCreated(extSource));
		return getExtSourcesManagerImpl().createExtSource(sess, extSource, attributes);
	}

	@Override
	public void deleteExtSource(PerunSession sess, ExtSource extSource) throws ExtSourceAlreadyRemovedException {
		getExtSourcesManagerImpl().deleteExtSource(sess, extSource);
		getPerunBl().getAuditer().log(sess, new ExtSourceDeleted(extSource));
	}

	@Override
	public ExtSource getExtSourceById(PerunSession sess, int id) throws ExtSourceNotExistsException {
		return getExtSourcesManagerImpl().getExtSourceById(sess, id);
	}

	@Override
	public ExtSource getExtSourceByName(PerunSession sess, String name) throws ExtSourceNotExistsException {
		return getExtSourcesManagerImpl().getExtSourceByName(sess, name);
	}

	@Override
	public List<ExtSource> getVoExtSources(PerunSession sess, Vo vo) {
		return getExtSourcesManagerImpl().getVoExtSources(sess, vo);
	}

	@Override
	public List<ExtSource> getGroupExtSources(PerunSession sess, Group group) {
		return getExtSourcesManagerImpl().getGroupExtSources(sess, group);
	}

	@Override
	public List<ExtSource> getExtSources(PerunSession sess) {
		return getExtSourcesManagerImpl().getExtSources(sess);
	}
	@Override
	public void addExtSource(PerunSession sess, Vo vo, ExtSource source) throws ExtSourceAlreadyAssignedException {
		getExtSourcesManagerImpl().addExtSource(sess, vo, source);
		getPerunBl().getAuditer().log(sess, new ExtSourceAddedToVo(source, vo));
	}

	@Override
	public void addExtSource(PerunSession sess, Group group, ExtSource source) throws ExtSourceAlreadyAssignedException {
		getExtSourcesManagerImpl().addExtSource(sess, group, source);
		getPerunBl().getAuditer().log(sess, new ExtSourceAddedToGroup(source, group));
	}

	@Override
	public ExtSource checkOrCreateExtSource(PerunSession sess, String extSourceName, String extSourceType) {
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
	public void removeExtSource(PerunSession sess, Vo vo, ExtSource source) throws ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		List<Group> groupsWithAssignedExtSource = getPerunBl().getGroupsManagerBl().getGroupsWithAssignedExtSourceInVo(sess, source, vo);
		for(Group group: groupsWithAssignedExtSource) {
			getPerunBl().getExtSourcesManagerBl().removeExtSource(sess, group, source);
		}

		getExtSourcesManagerImpl().removeExtSource(sess, vo, source);
		getPerunBl().getAuditer().log(sess,new ExtSourceRemovedFromVo(source, vo));
	}

	@Override
	public void removeExtSource(PerunSession sess, Group group, ExtSource source) throws ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		getExtSourcesManagerImpl().removeExtSource(sess, group, source);
		getPerunBl().getAuditer().log(sess,new ExtSourceRemovedFromGroup(source, group));
	}

	@Override
	public List<User> getInvalidUsers(PerunSession sess, ExtSource source) {
		List<Integer> usersIds;
		List<User> invalidUsers = new ArrayList<>();

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
	public void checkExtSourceExists(PerunSession sess, ExtSource extSource) throws ExtSourceNotExistsException {
		getExtSourcesManagerImpl().checkExtSourceExists(sess, extSource);
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	@Override
	public CandidateSync getCandidate(PerunSession sess, ExtSource source, String login) throws CandidateNotExistsException, ExtSourceUnsupportedOperationException {
		// Get the subject from the extSource
		Map<String, String> subject;
		try {
			subject = ((ExtSourceSimpleApi) source).getSubjectByLogin(login);
		} catch (SubjectNotExistsException e) {
			throw new CandidateNotExistsException("Searched candidate with login [" + login + "] does not exist");
		}

		if (subject == null) {
			throw new CandidateNotExistsException("Candidate with login [" + login + "] not exists");
		}

		return this.getCandidate(sess, subject, source, login);
	}

	@Override
	public CandidateSync getCandidate(PerunSession perunSession, Map<String,String> subjectData, ExtSource source, String login) {
		if(login == null || login.isEmpty()) throw new InternalErrorException("Login can't be empty or null.");
		if(subjectData == null || subjectData.isEmpty()) throw new InternalErrorException("Subject data can't be null or empty, at least login there must exists.");

		// New Canddate
		CandidateSync candidateSync = new CandidateSync();

		// Prepare userExtSource object
		UserExtSource userExtSource = new UserExtSource();
		userExtSource.setExtSource(source);
		userExtSource.setLogin(login);

		//If first name of candidate is not in format of name, set null instead
		candidateSync.setFirstName(subjectData.get("firstName"));
		if(candidateSync.getFirstName() != null) {
			Matcher name = namePattern.matcher(candidateSync.getFirstName());
			if(!name.matches()) candidateSync.setFirstName(null);
		}
		//If last name of candidate is not in format of name, set null instead
		candidateSync.setLastName(subjectData.get("lastName"));
		if(candidateSync.getLastName()!= null) {
			Matcher name = namePattern.matcher(candidateSync.getLastName());
			if(!name.matches()) candidateSync.setLastName(null);
		}
		candidateSync.setMiddleName(subjectData.get("middleName"));
		candidateSync.setTitleAfter(subjectData.get("titleAfter"));
		candidateSync.setTitleBefore(subjectData.get("titleBefore"));

		// Set service user
		if(subjectData.get("isServiceUser") == null) {
			candidateSync.setServiceUser(false);
		} else {
			String isServiceUser = subjectData.get("isServiceUser");
			candidateSync.setServiceUser(isServiceUser.equals("true"));
		}

		//Set sponsored user
		if(subjectData.get("isSponsoredUser") == null) {
			candidateSync.setSponsoredUser(false);
		} else {
			String isSponsoredUser = subjectData.get("isSponsoredUser");
			candidateSync.setSponsoredUser(isSponsoredUser.equals("true"));
		}

		// Filter attributes
		Map<String, String> attributes = new HashMap<>();
		for (String attrName: subjectData.keySet()) {
			// Allow only users and members attributes
			// FIXME volat metody z attributesManagera nez kontrolovat na zacatek jmena
			if (attrName.startsWith(AttributesManager.NS_MEMBER_ATTR) || attrName.startsWith(AttributesManager.NS_USER_ATTR)) {
				attributes.put(attrName, subjectData.get(attrName));
			}
		}

		candidateSync.setRichUserExtSource(new RichUserExtSource(userExtSource, new ArrayList<>()));
		candidateSync.setAdditionalRichUserExtSources(Utils.extractAdditionalUserExtSources(perunSession, subjectData));
		candidateSync.setAttributes(attributes);

		return candidateSync;
	}

	@Override
	public CandidateGroup generateCandidateGroup(PerunSession perunSession, Map<String,String> groupSubjectData, ExtSource source, String loginPrefix) {
		if(groupSubjectData == null) throw new InternalErrorException("Group subject data cannot be null.");
		if(groupSubjectData.isEmpty()) throw new InternalErrorException("Group subject data cannot be empty, at least group name has to exists.");
		if(source == null) throw new InternalErrorException("ExtSource cannot be null while generating CandidateGroup");

		CandidateGroup candidateGroup = new CandidateGroup();

		candidateGroup.setExtSource(source);
		candidateGroup.asGroup().setName(groupSubjectData.get(GroupsManagerBlImpl.GROUP_NAME));
		candidateGroup.setLogin(loginPrefix + groupSubjectData.get(GroupsManagerBlImpl.GROUP_LOGIN));

		if(candidateGroup.getLogin() == null || candidateGroup.getLogin().isEmpty()) {
			throw new InternalErrorException("Group subject data has to contain valid group login!");
		}

		// Check if the group name is not null and if it is in valid format.
		if(candidateGroup.asGroup().getName() != null) {
			try {
				Utils.validateGroupName(candidateGroup.asGroup().getName());
			} catch (InvalidGroupNameException e) {
				throw new InternalErrorException("Group subject data has to contain valid group name!", e);
			}
		} else {
			throw new InternalErrorException("group name cannot be null in Group subject data!");
		}

		if(groupSubjectData.get(GroupsManagerBlImpl.PARENT_GROUP_LOGIN) != null) {
			candidateGroup.setParentGroupLogin(loginPrefix + groupSubjectData.get(GroupsManagerBlImpl.PARENT_GROUP_LOGIN));
		}
		candidateGroup.asGroup().setDescription(groupSubjectData.get(GroupsManagerBlImpl.GROUP_DESCRIPTION));

		groupSubjectData.entrySet().stream()
			.filter(entry -> !GROUP_SYNC_DEFAULT_DATA.contains(entry.getKey()))
			.forEach(entry -> candidateGroup.addAdditionalAttribute(entry.getKey(), entry.getValue()));

		return candidateGroup;
	}

	@Override
	public List<CandidateGroup> generateCandidateGroups(PerunSession perunSession, List<Map<String,String>> subjectsData, ExtSource source, String loginPrefix) {
		List<CandidateGroup> candidateGroups= new ArrayList<>();

		for (Map<String, String> subjectData : subjectsData) {
			candidateGroups.add(generateCandidateGroup(perunSession, subjectData, source, loginPrefix));
		}

		return candidateGroups;
	}

	@Override
	public void checkExtSourceAssignedToVo(PerunSession sess, ExtSource extSource, int voId) throws ExtSourceNotAssignedException, VoNotExistsException {
		Vo vo = getPerunBl().getVosManagerBl().getVoById(sess, voId);
		List<ExtSource> voExtSources = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);

		if(!voExtSources.contains(extSource)) throw new ExtSourceNotAssignedException("ExtSource " + extSource + " is not assigned to vo " + vo);
	}

	@Override
	public void loadExtSourcesDefinitions(PerunSession sess) {
		getExtSourcesManagerImpl().loadExtSourcesDefinitions(sess);
	}

	@Override
	public Map<String, String> getAttributes(ExtSource extSource) {
		return getExtSourcesManagerImpl().getAttributes(extSource);
	}
}
