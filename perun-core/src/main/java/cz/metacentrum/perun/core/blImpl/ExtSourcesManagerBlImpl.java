package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceAddedToGroup;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceAddedToVo;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceCreated;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceDeleted;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceRemovedFromGroup;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceRemovedFromVo;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.CandidateGroup;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
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
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.ExtSourcesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import cz.metacentrum.perun.core.implApi.ExtSourcesManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExtSourcesManagerBlImpl implements ExtSourcesManagerBl {
	final static Logger log = LoggerFactory.getLogger(ExtSourcesManagerBlImpl.class);
	// \\p{L} means any unicode char
	private static final Pattern namePattern = Pattern.compile("^[\\p{L} ,.0-9_'-]+$");
	private static final Pattern groupNamePattern = Pattern.compile(GroupsManager.GROUP_SHORT_NAME_REGEXP);

	private final ExtSourcesManagerImplApi extSourcesManagerImpl;
	private PerunBl perunBl;
	private final AtomicBoolean initialized = new AtomicBoolean(false);


	public ExtSourcesManagerBlImpl(ExtSourcesManagerImplApi extSourcesManagerImpl) {
		this.extSourcesManagerImpl = extSourcesManagerImpl;
	}

	@Override
	public void initialize(PerunSession sess) {
		if (!this.initialized.compareAndSet(false, true)) return;
		this.extSourcesManagerImpl.initialize(sess, perunBl);
		cacheExtSourcesInMemory(sess);
	}

	private Map<Integer,ExtSource> extSourcesByIdMap = new HashMap<>();
	private Map<String,ExtSource> extSourcesByNameMap = new HashMap<>();
	
	private void cacheExtSourcesInMemory(PerunSession sess) {
		Map<Integer,ExtSource> extSourcesByIdMap = new HashMap<>();
		Map<String,ExtSource> extSourcesByNameMap = new HashMap<>();
		// read all ExtSources from database
		List<ExtSource> extSources = getExtSourcesManagerImpl().getExtSources(sess);
		// cache them in hashmaps
		for(ExtSource extSource : extSources) {
			extSourcesByIdMap.put(extSource.getId(), extSource);
			extSourcesByNameMap.put(extSource.getName(), extSource);
		}
		this.extSourcesByIdMap = extSourcesByIdMap;
		this.extSourcesByNameMap = extSourcesByNameMap;
	}

	@Override
	public void destroy() {
		this.extSourcesManagerImpl.destroy();
	}

	@Override
	public ExtSource createExtSource(PerunSession sess, ExtSource extSource, Map<String, String> attributes) throws InternalErrorException, ExtSourceExistsException {
		getPerunBl().getAuditer().log(sess, new ExtSourceCreated(extSource));
		ExtSource extSourceImpl = getExtSourcesManagerImpl().createExtSource(sess, extSource, attributes);
		extSourcesByIdMap.put(extSourceImpl.getId(), extSource);
		extSourcesByNameMap.put(extSourceImpl.getName(), extSource);
		return extSourceImpl;
	}

	@Override
	public void deleteExtSource(PerunSession sess, ExtSource extSource) throws InternalErrorException, ExtSourceAlreadyRemovedException {
		getExtSourcesManagerImpl().deleteExtSource(sess, extSource);
		cacheExtSourcesInMemory(sess);
		getPerunBl().getAuditer().log(sess, new ExtSourceDeleted(extSource));
	}

	@Override
	public ExtSource getExtSourceById(PerunSession sess, int id) throws InternalErrorException, ExtSourceNotExistsException {
		ExtSource extSource = extSourcesByIdMap.get(id);
		if (extSource == null) throw new ExtSourceNotExistsException("ExtSource with ID=" + id + " not exists");
		return extSource;
	}

	@Override
	public ExtSource getExtSourceByName(PerunSession sess, String name) throws InternalErrorException, ExtSourceNotExistsException {
		ExtSource extSource = extSourcesByNameMap.get(name);
		if (extSource == null) throw new ExtSourceNotExistsException("ExtSource with name =" + name + " not exists");
		return extSource;
	}

	@Override
	public List<ExtSource> getVoExtSources(PerunSession sess, Vo vo) throws InternalErrorException {
		List<Integer> ids = getExtSourcesManagerImpl().getVoExtSourcesIds(sess, vo);
		return ids.stream().map(id -> extSourcesByIdMap.get(id)).collect(Collectors.toList());
	}

	@Override
	public List<ExtSource> getGroupExtSources(PerunSession sess, Group group) throws InternalErrorException {
		List<Integer> ids = getExtSourcesManagerImpl().getGroupExtSourcesIds(sess, group);
		return ids.stream().map(id -> extSourcesByIdMap.get(id)).collect(Collectors.toList());
	}

	@Override
	public List<ExtSource> getExtSources(PerunSession sess) throws InternalErrorException {
		return new ArrayList<>(extSourcesByIdMap.values());
	}
	
	@Override
	public void addExtSourceToVo(PerunSession sess, Vo vo, ExtSource source) throws InternalErrorException, ExtSourceAlreadyAssignedException {
		getExtSourcesManagerImpl().addExtSourceToVo(sess, vo, source);
		getPerunBl().getAuditer().log(sess, new ExtSourceAddedToVo(source, vo));
	}

	@Override
	public void addExtSourceToGroup(PerunSession sess, Group group, ExtSource source) throws InternalErrorException, ExtSourceAlreadyAssignedException {
		getExtSourcesManagerImpl().addExtSourceToGroup(sess, group, source);
		getPerunBl().getAuditer().log(sess, new ExtSourceAddedToGroup(source, group));
	}

	@Override
	public ExtSource checkOrCreateExtSource(PerunSession sess, String extSourceName, String extSourceType) throws InternalErrorException {
		// Check if the extSource exists
		try {
			return getExtSourceByName(sess, extSourceName);
		} catch (ExtSourceNotExistsException e) {
			// extSource doesn't exist, so create new one
			ExtSource extSource = new ExtSource(extSourceName, extSourceType);
			try {
				return this.createExtSource(sess, extSource, null);
			} catch (ExtSourceExistsException e1) {
				throw new ConsistencyErrorException("Creating existing extSource", e1);
			}
		}
	}

	@Override
	public void removeExtSourceFromVo(PerunSession sess, Vo vo, ExtSource source) throws InternalErrorException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		List<Group> groupsWithAssignedExtSource = getPerunBl().getGroupsManagerBl().getGroupsWithAssignedExtSourceInVo(sess, source, vo);
		for(Group group: groupsWithAssignedExtSource) {
			getPerunBl().getExtSourcesManagerBl().removeExtSourceFromGroup(sess, group, source);
		}
		getExtSourcesManagerImpl().removeExtSourceFromVo(sess, vo, source);
		getPerunBl().getAuditer().log(sess,new ExtSourceRemovedFromVo(source, vo));
	}

	@Override
	public void removeExtSourceFromGroup(PerunSession sess, Group group, ExtSource source) throws InternalErrorException, ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		getExtSourcesManagerImpl().removeExtSourceFromGroup(sess, group, source);
		getPerunBl().getAuditer().log(sess,new ExtSourceRemovedFromGroup(source, group));
	}

	@Override
	public List<User> getInvalidUsers(PerunSession sess, ExtSource source) throws InternalErrorException {
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
	public void checkExtSourceExists(PerunSession sess, ExtSource extSource) throws InternalErrorException, ExtSourceNotExistsException {
		getExtSourceById(sess, extSource.getId());
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	@Override
	public Candidate getCandidate(PerunSession sess, ExtSource source, String login) throws InternalErrorException, CandidateNotExistsException, ExtSourceUnsupportedOperationException {
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
	public Candidate getCandidate(PerunSession perunSession, Map<String,String> subjectData, ExtSource source, String login) throws InternalErrorException {
		if(login == null || login.isEmpty()) throw new InternalErrorException("Login can't be empty or null.");
		if(subjectData == null || subjectData.isEmpty()) throw new InternalErrorException("Subject data can't be null or empty, at least login there must exists.");

		// New Canddate
		Candidate candidate = new Candidate();

		// Prepare userExtSource object
		UserExtSource userExtSource = new UserExtSource();
		userExtSource.setExtSource(source);
		userExtSource.setLogin(login);

		// Set the userExtSource
		candidate.setUserExtSource(userExtSource);

		//If first name of candidate is not in format of name, set null instead
		candidate.setFirstName(subjectData.get("firstName"));
		if(candidate.getFirstName() != null) {
			Matcher name = namePattern.matcher(candidate.getFirstName());
			if(!name.matches()) candidate.setFirstName(null);
		}
		//If last name of candidate is not in format of name, set null instead
		candidate.setLastName(subjectData.get("lastName"));
		if(candidate.getLastName()!= null) {
			Matcher name = namePattern.matcher(candidate.getLastName());
			if(!name.matches()) candidate.setLastName(null);
		}
		candidate.setMiddleName(subjectData.get("middleName"));
		candidate.setTitleAfter(subjectData.get("titleAfter"));
		candidate.setTitleBefore(subjectData.get("titleBefore"));

		// Set service user
		if(subjectData.get("isServiceUser") == null) {
			candidate.setServiceUser(false);
		} else {
			String isServiceUser = subjectData.get("isServiceUser");
			candidate.setServiceUser(isServiceUser.equals("true"));
		}

		//Set sponsored user
		if(subjectData.get("isSponsoredUser") == null) {
			candidate.setSponsoredUser(false);
		} else {
			String isSponsoredUser = subjectData.get("isSponsoredUser");
			candidate.setSponsoredUser(isSponsoredUser.equals("true"));
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
		List<UserExtSource> additionalUserExtSources = Utils.extractAdditionalUserExtSources(perunSession, subjectData);
		candidate.setAdditionalUserExtSources(additionalUserExtSources);
		candidate.setAttributes(attributes);

		return candidate;
	}

	@Override
	public CandidateGroup generateCandidateGroup(PerunSession perunSession, Map<String,String> groupSubjectData, ExtSource source) throws InternalErrorException {
		if(groupSubjectData == null) throw new InternalErrorException("Group subject data cannot be null.");
		if(groupSubjectData.isEmpty()) throw new InternalErrorException("Group subject data cannot be empty, at least group name has to exists.");
		if(source == null) throw new InternalErrorException("ExtSource cannot be null while generating CandidateGroup");

		CandidateGroup candidateGroup = new CandidateGroup();

		candidateGroup.setExtSource(source);
		candidateGroup.asGroup().setName(groupSubjectData.get(GroupsManagerBlImpl.GROUP_NAME));

		// Check if the group name is not null and if it is in valid format.
		if(candidateGroup.asGroup().getName() != null) {
			Matcher name = groupNamePattern.matcher(candidateGroup.asGroup().getName());
			if(!name.matches()) throw new InternalErrorException("Group subject data has to contains valid group name!");
		} else {
			throw new InternalErrorException("group name cannot be null in Group subject data!");
		}

		candidateGroup.setParentGroupName(groupSubjectData.get(GroupsManagerBlImpl.PARENT_GROUP_NAME));
		candidateGroup.asGroup().setDescription(groupSubjectData.get(GroupsManagerBlImpl.GROUP_DESCRIPTION));

		return candidateGroup;
	}

	@Override
	public List<CandidateGroup> generateCandidateGroups(PerunSession perunSession, List<Map<String,String>> subjectsData, ExtSource source) throws InternalErrorException {
		List<CandidateGroup> candidateGroups= new ArrayList<>();

		for (Map<String, String> subjectData : subjectsData) {
			candidateGroups.add(generateCandidateGroup(perunSession, subjectData, source));
		}

		return candidateGroups;
	}

	@Override
	public DataSource getDataSource(String poolName) {
		return getExtSourcesManagerImpl().getDataSource(poolName);
	}

	@Override
	public void checkExtSourceAssignedToVo(PerunSession sess, ExtSource extSource, int voId) throws InternalErrorException, ExtSourceNotAssignedException, VoNotExistsException {
		Vo vo = getPerunBl().getVosManagerBl().getVoById(sess, voId);
		if(!getExtSourcesManagerImpl().getVoExtSourcesIds(sess, vo).contains(extSource.getId())) throw new ExtSourceNotAssignedException("ExtSource " + extSource + " is not assigned to vo " + vo);
	}

	@Override
	public void loadExtSourcesDefinitions(PerunSession sess) {
		getExtSourcesManagerImpl().loadExtSourcesDefinitions(sess);
		cacheExtSourcesInMemory(sess);
	}

	@Override
	public Map<String, String> getAttributes(ExtSource extSource) throws InternalErrorException {
		return getExtSourcesManagerImpl().getAttributes(extSource);
	}
}
