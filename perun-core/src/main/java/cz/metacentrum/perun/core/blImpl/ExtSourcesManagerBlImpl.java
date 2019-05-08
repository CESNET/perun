package cz.metacentrum.perun.core.blImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceAddedToGroup;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceAddedToVo;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceCreated;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceDeleted;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceRemovedFromGroup;
import cz.metacentrum.perun.audit.events.ExtSourcesManagerEvents.ExtSourceRemovedFromVo;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunBeanProcessingPool;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Group;
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
	private Integer maxConcurrentExtSourcesToSynchronize;
	private final PerunBeanProcessingPool<ExtSource> poolOfExtSourcesToBeSynchronized;
	private final ArrayList<ExtSourceSynchronizerThread> extSourceSynchronizerThreads;


	public ExtSourcesManagerBlImpl(ExtSourcesManagerImplApi extSourcesManagerImpl) {
		this.extSourcesManagerImpl = extSourcesManagerImpl;
		this.extSourceSynchronizerThreads = new ArrayList<>();
		this.poolOfExtSourcesToBeSynchronized = new PerunBeanProcessingPool<>();
		//set maximum concurrent extSources to synchronize by property
		this.maxConcurrentExtSourcesToSynchronize = BeansUtils.getCoreConfig().getExtSourceMaxConcurentExtSourcesToSynchronize();
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
		// New Canddate
		Candidate candidate = new Candidate();

		// Prepare userExtSource object
		UserExtSource userExtSource = new UserExtSource();
		userExtSource.setExtSource(source);
		userExtSource.setLogin(login);

		// Set the userExtSource
		candidate.setUserExtSource(userExtSource);

		// Get the subject from the extSource
		Map<String, String> subject = null;
		try {
			subject = ((ExtSourceSimpleApi) source).getSubjectByLogin(login);
		} catch (SubjectNotExistsException e) {
			throw new CandidateNotExistsException(login);
		}

		if (subject == null) {
			throw new CandidateNotExistsException("Candidate with login [" + login + "] not exists");
		}

		//If first name of candidate is not in format of name, set null instead
		candidate.setFirstName(subject.get("firstName"));
		if(candidate.getFirstName() != null) {
			Matcher name = namePattern.matcher(candidate.getFirstName());
			if(!name.matches()) candidate.setFirstName(null);
		}
		//If last name of candidate is not in format of name, set null instead
		candidate.setLastName(subject.get("lastName"));
		if(candidate.getLastName()!= null) {
			Matcher name = namePattern.matcher(candidate.getLastName());
			if(!name.matches()) candidate.setLastName(null);
		}
		candidate.setMiddleName(subject.get("middleName"));
		candidate.setTitleAfter(subject.get("titleAfter"));
		candidate.setTitleBefore(subject.get("titleBefore"));

		//Set service user
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

		//Set sponsored user
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

		// Additional userExtSources
		List<UserExtSource> additionalUserExtSources = new ArrayList<UserExtSource>();

		// Filter attributes
		Map<String, String> attributes = new HashMap<String, String>();
		for (String attrName: subject.keySet()) {
			// Allow only users and members attributes
			// FIXME volat metody z attributesManagera nez kontrolovat na zacatek jmena
			if (attrName.startsWith(AttributesManager.NS_MEMBER_ATTR) || attrName.startsWith(AttributesManager.NS_USER_ATTR)) {
				attributes.put(attrName, subject.get(attrName));
			} else if (attrName.startsWith(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING)) {
				if(subject.get(attrName) == null) continue; //skip null additional ext sources
				// Add additionalUserExtSources
				String[] userExtSourceRaw =  subject.get(attrName).split("\\|"); // Entry contains extSourceName|extSourceType|extLogin[|LoA]
				log.debug("Processing additionalUserExtSource {}",  subject.get(attrName));

				//Check if the array has at least 3 parts, this is protection against outOfBoundException
				if(userExtSourceRaw.length < 3) {
					throw new InternalErrorException("There is missing some mandatory part of additional user extSource value when processing it - '" + attrName + "'");
				}

				String additionalExtSourceName = userExtSourceRaw[0];
				String additionalExtSourceType = userExtSourceRaw[1];
				String additionalExtLogin = userExtSourceRaw[2];
				int additionalExtLoa = 0;
				//Loa is not mandatory argument
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
					//add additional user extSource
					additionalUserExtSources.add(new UserExtSource(additionalExtSource, additionalExtLoa, additionalExtLogin));
				}
			}
		}

		candidate.setAdditionalUserExtSources(additionalUserExtSources);
		candidate.setAttributes(attributes);

		return candidate;
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

		//Set service user
		if(subjectData.get("isServiceUser") == null) {
			candidate.setServiceUser(false);
		} else {
			String isServiceUser = subjectData.get("isServiceUser");
			if(isServiceUser.equals("true")) {
				candidate.setServiceUser(true);
			} else {
				candidate.setServiceUser(false);
			}
		}

		//Set sponsored user
		if(subjectData.get("isSponsoredUser") == null) {
			candidate.setSponsoredUser(false);
		} else {
			String isSponsoredUser = subjectData.get("isSponsoredUser");
			if(isSponsoredUser.equals("true")) {
				candidate.setSponsoredUser(true);
			} else {
				candidate.setSponsoredUser(false);
			}
		}

		// Additional userExtSources
		List<UserExtSource> additionalUserExtSources = new ArrayList<UserExtSource>();

		// Filter attributes
		Map<String, String> attributes = new HashMap<String, String>();
		for (String attrName: subjectData.keySet()) {
			// Allow only users and members attributes
			// FIXME volat metody z attributesManagera nez kontrolovat na zacatek jmena
			if (attrName.startsWith(AttributesManager.NS_MEMBER_ATTR) || attrName.startsWith(AttributesManager.NS_USER_ATTR)) {
				attributes.put(attrName, subjectData.get(attrName));
			} else if (attrName.startsWith(ExtSourcesManagerImpl.USEREXTSOURCEMAPPING)) {
				if(subjectData.get(attrName) == null) continue; //skip null additional ext sources
				// Add additionalUserExtSources
				String[] userExtSourceRaw =  subjectData.get(attrName).split("\\|"); // Entry contains extSourceName|extSourceType|extLogin[|LoA]
				log.debug("Processing additionalUserExtSource {}",  subjectData.get(attrName));

				//Check if the array has at least 3 parts, this is protection against outOfBoundException
				if(userExtSourceRaw.length < 3) {
					throw new InternalErrorException("There is missing some mandatory part of additional user extSource value when processing it - '" + attrName + "'");
				}

				String additionalExtSourceName = userExtSourceRaw[0];
				String additionalExtSourceType = userExtSourceRaw[1];
				String additionalExtLogin = userExtSourceRaw[2];
				int additionalExtLoa = 0;
				//Loa is not mandatory argument
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
						additionalExtSource = getPerunBl().getExtSourcesManagerBl().getExtSourceByName(perunSession, additionalExtSourceName);
					} catch (ExtSourceNotExistsException e) {
						try {
							// Create new one if not exists
							additionalExtSource = new ExtSource(additionalExtSourceName, additionalExtSourceType);
							additionalExtSource = getPerunBl().getExtSourcesManagerBl().createExtSource(perunSession, additionalExtSource, null);
						} catch (ExtSourceExistsException e1) {
							throw new ConsistencyErrorException("Creating existin extSource: " + additionalExtSourceName);
						}
					}
					//add additional user extSource
					additionalUserExtSources.add(new UserExtSource(additionalExtSource, additionalExtLoa, additionalExtLogin));
				}
			}
		}

		candidate.setAdditionalUserExtSources(additionalUserExtSources);
		candidate.setAttributes(attributes);

		return candidate;
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

	public synchronized void synchronizeExtSource(PerunSession sess, ExtSource extSource) throws InternalErrorException {
		try {
			//Get subjects from extSource
			List<Map<String, String>> subjects = getSubjectsFromExtSource(extSource);

			for (Map<String, String> subject : subjects) {
				Candidate candidate = getCandidate(sess, subject, extSource, subject.get("login"));
				getPerunBl().getUsersManagerBl().addCandidateToPool(candidate);
			}
			log.info("Synchronization for {} users was scheduled.", subjects.size());
		} finally {
			//Close open extSource if they support this operation
			try {
				((ExtSourceSimpleApi) extSource).close();
			} catch (ExtSourceUnsupportedOperationException e) {
				// ExtSource doesn't support that functionality, so silently skip it.
			} catch (InternalErrorException e) {
				log.warn("Can't close extSource connection. Cause: {}", e);
			}
		}
	}

	public void forceExtSourceSynchronization(PerunSession sess, ExtSource extSource) throws InternalErrorException {
		log.info("Force synchronization for ExtSource: {} started.", extSource);

		int numberOfNewlyRemovedThreads = removeInteruptedThreads();
		int numberOfNewlyCreatedThreads = initializeNewSynchronizationThreads(sess);

		this.perunBl.getUsersManagerBl().reinitializeUserSynchronizerThreads(sess);

		if (extSourcesManagerImpl.getExtSourcesToSynchronize(sess).contains(extSource)) {
			poolOfExtSourcesToBeSynchronized.putJobIfAbsent(extSource, true);
		} else {
			log.warn("Synchronization for ExtSource: {} wasn't enable.", extSource);
		}

		// Save state of synchronization to the info log
		log.info("ExtSource synchronization method ends with these states: " +
				"'number of newly removed threads'='" + numberOfNewlyRemovedThreads + "', " +
				"'number of newly created threads'='" + numberOfNewlyCreatedThreads + "', " +
				"'number of newly added extSource to the pool'='1', " +
				"'right now synchronized extSources'='" + poolOfExtSourcesToBeSynchronized.getRunningJobs() + "', " +
				"'right now waiting extSources'='" + poolOfExtSourcesToBeSynchronized.getWaitingJobs() + "'.");
	}


	public synchronized void synchronizeExtSources(PerunSession sess) throws InternalErrorException {
		LocalDateTime localDateTime = LocalDateTime.now();
		String pattern = "^(([0-1][0-9])|(2[0-3])):[0-5][0,5]$";

		int numberOfNewlyRemovedThreads = removeInteruptedThreads();

		int numberOfNewlyCreatedThreads = initializeNewSynchronizationThreads(sess);

		List<ExtSource> extSources = extSourcesManagerImpl.getExtSourcesToSynchronize(sess);

		int numberOfNewlyAddedExtSource = 0;
		for (ExtSource extSource : extSources) {
			Map<String, String> attributes = extSourcesManagerImpl.getAttributes(extSource);
			String[] synchronizationTimes = attributes.get(ExtSourcesManager.EXTSOURCE_SYNCHRONIZATION_TIMES_ATTRNAME).split(",");
			String time = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
			for (String synchronizationTime : synchronizationTimes) {
				if (synchronizationTime.matches(pattern) && synchronizationTime.equals(time)) {
					if (poolOfExtSourcesToBeSynchronized.putJobIfAbsent(extSource, false)) {
						numberOfNewlyAddedExtSource++;
						log.debug("ExtSource {} was added to the pool of extSources waiting for synchronization.", extSource);
						continue;
					} else {
						log.debug("ExtSource {} synchronization is already running.", extSource);
					}
				}
			}
		}

		// Save state of synchronization to the info log
		log.info("SynchronizeExtSources method ends with these states: " +
				"'number of newly removed threads'='" + numberOfNewlyRemovedThreads + "', " +
				"'number of newly created threads'='" + numberOfNewlyCreatedThreads + "', " +
				"'number of newly added extSources to the pool'='" + numberOfNewlyAddedExtSource + "', " +
				"'right now synchronized extSources'='" + poolOfExtSourcesToBeSynchronized.getRunningJobs() + "', " +
				"'right now waiting extSources'='" + poolOfExtSourcesToBeSynchronized.getWaitingJobs() + "'.");
	}


	public List<String> getOverwriteUserAttributeList(ExtSource extSource) throws InternalErrorException {
		Map<String, String> extSourceAttributes = getPerunBl().getExtSourcesManagerBl().getAttributes(extSource);
		String[] overwriteUserAttributes = extSourceAttributes.get(ExtSourcesManager.OVERWRITEATTRIBUTES_ATTRNAME).split(",");
		return  Arrays.asList(overwriteUserAttributes);
	}

	//----------- PRIVATE METHODS

	/**
	 * Returns all subjects from extSource
	 *
	 * @param extSource ExtSource
	 * @return List of subjects from extSource
	 * @throws InternalErrorException
	 */
	private List<Map<String, String>> getSubjectsFromExtSource(ExtSource extSource) throws InternalErrorException {
		List<Map<String, String>> subjects;
		try {
			subjects = ((ExtSourceSimpleApi) extSource).getUsersSubjects();
		} catch (ExtSourceUnsupportedOperationException e) {
			throw new InternalErrorException("ExtSource " + extSource.getName() + " doesn't support getSubjects", e);
		}
		return subjects;
	}

	/**
	 * Starts new threads if there is place and retruns count of newly created threads
	 * @param sess PerunSession
	 * @return Count of newly started threads
	 * @throws InternalErrorException
	 */
	private int initializeNewSynchronizationThreads(PerunSession sess) throws InternalErrorException {
		int numberOfNewlyCreatedThreads = 0;

		// Start new threads if there is place for them
		while(extSourceSynchronizerThreads.size() < maxConcurrentExtSourcesToSynchronize) {
			ExtSourceSynchronizerThread thread = new ExtSourceSynchronizerThread(sess);
			thread.start();
			extSourceSynchronizerThreads.add(thread);
			numberOfNewlyCreatedThreads++;
			log.debug("New thread for extSources synchronization started.");
		}
		return numberOfNewlyCreatedThreads;
	}

	/**
	 * This function removed interupted threads
	 *
	 * @return Number of removed threads
	 */
	private int removeInteruptedThreads() {
		int numberOfNewlyRemovedThreads = 0;

		// Get the default synchronization timeout from the configuration file
		int timeout = BeansUtils.getCoreConfig().getExtSourceSynchronizationTimeout();

		Iterator<ExtSourceSynchronizerThread> threadIterator = extSourceSynchronizerThreads.iterator();

		while(threadIterator.hasNext()) {
			ExtSourceSynchronizerThread thread = threadIterator.next();
			long threadStart = thread.getStartTime();
			//If thread start time is 0, this thread is waiting for another job, skip it
			if(threadStart == 0) continue;

			long timeDiff = System.currentTimeMillis() - threadStart;
			//If thread was interrupted by anything, remove it from the pool of active threads
			if (thread.isInterrupted()) {
				numberOfNewlyRemovedThreads++;
				threadIterator.remove();
			} else if(timeDiff/1000/60 > timeout) {
				// If the time is greater than timeout set in the configuration file (in minutes), interrupt and remove this thread from pool
				log.error("One of threads was interrupted because of timeout!");
				thread.interrupt();
				threadIterator.remove();
				numberOfNewlyRemovedThreads++;
			}
		}

		return numberOfNewlyRemovedThreads;
	}


	//----------- PRIVATE CLASSESS

	private class ExtSourceSynchronizerThread extends Thread {
		// all synchronization runs under synchronizer identity.
		private final PerunPrincipal pp = new PerunPrincipal("perunSynchronizer", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		private final PerunBl perunBl;
		private final PerunSession sess;
		private volatile long startTime;

		public ExtSourceSynchronizerThread(PerunSession sess) throws InternalErrorException {
			// take only reference to perun
			this.perunBl = (PerunBl) sess.getPerun();
			this.sess = perunBl.getPerunSession(pp, new PerunClient());
			//Default settings of not running thread (waiting for another group)
			this.startTime = 0;
		}

		public void run() {
			while (true) {
				//Set thread to default state (waiting for another group to synchronize)
				this.setThreadToDefaultState();

				//If this thread was interrupted, end it's running
				if(this.isInterrupted()) return;

				//Take another extSource from the pool to synchronize it
				ExtSource extSource = null;
				try {
					extSource = poolOfExtSourcesToBeSynchronized.takeJob();
				} catch (InterruptedException ex) {
					log.error("Thread was interrupted when trying to take another ExtSource to synchronize from pool", ex);
					//Interrupt this thread
					this.interrupt();
					return;
				}

				try {
					// Set the start time, so we can check the timeout of the thread
					startTime = System.currentTimeMillis();

					log.debug("Synchronization thread started synchronization for ExtSource {}.", extSource);

					//Synchronize ExtSource
					perunBl.getExtSourcesManagerBl().synchronizeExtSource(sess, extSource);

					log.debug("Synchronization thread for extSource {} has finished in {} ms.", extSource, System.currentTimeMillis() - startTime);
				} catch (Exception e) {
					log.error("Cannot synchronize extSource " + extSource, e);
				} finally {
					//Remove job from running jobs
					if(!poolOfExtSourcesToBeSynchronized.removeJob(extSource)) {
						log.error("Can't remove running job for object " + extSource + " from pool of running jobs because it is not containing it.");
					}

					log.debug("ExtSourceSynchronizerThread finished for extSource: {}", extSource);
				}
			}
		}

		public long getStartTime() {
			return startTime;
		}

		private void setThreadToDefaultState() {
			this.startTime = 0;
		}
	}

}
