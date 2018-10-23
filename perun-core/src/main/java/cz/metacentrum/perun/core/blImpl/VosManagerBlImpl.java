package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.VoManagerEvents.VoCreated;
import cz.metacentrum.perun.audit.events.VoManagerEvents.VoDeleted;
import cz.metacentrum.perun.audit.events.VoManagerEvents.VoUpdated;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.bl.MembersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import cz.metacentrum.perun.core.implApi.VosManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * VosManager business logic
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
@SuppressWarnings("deprecation")
public class VosManagerBlImpl implements VosManagerBl {

	private final static Logger log = LoggerFactory.getLogger(VosManagerBlImpl.class);

	private final VosManagerImplApi vosManagerImpl;
	private PerunBl perunBl;

	/**
	 * Constructor.
	 */
	public VosManagerBlImpl(VosManagerImplApi vosManagerImpl) {
		this.vosManagerImpl = vosManagerImpl;
	}

	@Override
	public List<Vo> getVos(PerunSession sess) throws InternalErrorException {
		return getVosManagerImpl().getVos(sess);
	}

	@Override
	public void deleteVo(PerunSession sess, Vo vo, boolean forceDelete) throws InternalErrorException, RelationExistsException {
		log.debug("Deleting vo {}", vo);

		try {
			List<Member> members = getPerunBl().getMembersManagerBl().getMembers(sess, vo);

			log.debug("Deleting vo {} members", vo);
			// Check if there are some members left
			if (members != null && members.size() > 0) {
				if (forceDelete) {
					getPerunBl().getMembersManagerBl().deleteAllMembers(sess, vo);
				} else throw new RelationExistsException("Vo vo=" + vo + " contains members");
			}

			log.debug("Removing vo {} resources and theirs attributes", vo);
			// Delete resources
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
			if ((resources.size() == 0) || forceDelete) {
				for (Resource resource : resources) {
					getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, resource);
					// Remove binding between service and resource
					List<Service> services = getPerunBl().getResourcesManagerBl().getAssignedServices(sess, resource);
					for (Service service : services) {
						getPerunBl().getResourcesManagerBl().removeService(sess, resource, service);
					}
					getPerunBl().getResourcesManagerBl().deleteResource(sess, resource);
				}
			} else {
				throw new RelationExistsException("Vo vo=" + vo + " contains resources");
			}

			log.debug("Removing vo {} groups", vo);
			// Delete all groups

			List<Group> groups = getPerunBl().getGroupsManagerBl().getGroups(sess, vo);
			if (groups.size() != 1) {
				if (groups.size() < 1) throw new ConsistencyErrorException("'members' group is missing");
				if (forceDelete) {
					getPerunBl().getGroupsManagerBl().deleteAllGroups(sess, vo);
				} else {
					throw new RelationExistsException("Vo vo=" + vo + " contains groups");
				}
			}

			// Finally delete binding between Vo and external source
			List<ExtSource> ess = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);
			log.debug("Deleting {} external sources binded to the vo {}", ess.size(), vo);
			for (ExtSource es : ess) {
				getPerunBl().getExtSourcesManagerBl().removeExtSource(sess, vo, es);
			}

			// Delete members group
			log.debug("Removing an administrators' group from the vo {}", vo);
			getPerunBl().getGroupsManagerBl().deleteMembersGroup(sess, vo);

			// delete all VO reserved logins from KDC
			List<Integer> list = getVosManagerImpl().getVoApplicationIds(sess, vo);
			for (Integer appId : list) {
				// for each application
				for (Pair<String, String> login : getVosManagerImpl().getApplicationReservedLogins(appId)) {
					// for all reserved logins - delete them in ext. system (e.g. KDC)
					try {
						// !!! left = namespace / right = login !!!
						getPerunBl().getUsersManagerBl().deletePassword(sess, login.getRight(), login.getLeft());
					} catch (LoginNotExistsException ex) {
						log.error("Login: {} not exists in namespace {} while deleting passwords", login.getRight(), login.getLeft());
					}
				}
			}
			// delete all VO reserved logins from DB
			getVosManagerImpl().deleteVoReservedLogins(sess, vo);

			// VO applications, submitted data and app_form are deleted on cascade with "deleteVo()"

			// Delete VO attributes
			getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, vo);

			// Delete all Vo tags (for resources in Vo)
			getPerunBl().getResourcesManagerBl().deleteAllResourcesTagsForVo(sess, vo);

		} catch (Exception ex) {
			throw new InternalErrorException(ex);
		}

		// Finally delete the VO
		getVosManagerImpl().deleteVo(sess, vo);
		getPerunBl().getAuditer().log(sess, new VoDeleted(vo));
	}

	@Override
	public void deleteVo(PerunSession sess, Vo vo) throws InternalErrorException, RelationExistsException {
		// delete VO only if it is completely empty
		this.deleteVo(sess, vo, false);
	}

	@Override
	public Vo createVo(PerunSession sess, Vo vo) throws VoExistsException, InternalErrorException {
		// Create entries in the DB and Grouper
		vo = getVosManagerImpl().createVo(sess, vo);
		getPerunBl().getAuditer().log(sess, new VoCreated(vo));

		try {
			// Create group containing VO members
			Group members = new Group(VosManager.MEMBERS_GROUP, VosManager.MEMBERS_GROUP_DESCRIPTION + " for VO " + vo.getName());
			getPerunBl().getGroupsManagerBl().createGroup(sess, vo, members);
			log.debug("Members group created, vo '{}'", vo);
		} catch (GroupExistsException e) {
			throw new ConsistencyErrorException("Group already exists", e);
		}

		// create empty application form
		getVosManagerImpl().createApplicationForm(sess, vo);

		//set creator as VO manager
		if (sess.getPerunPrincipal().getUser() != null) {
			try {
				addAdmin(sess, vo, sess.getPerunPrincipal().getUser());
			} catch (AlreadyAdminException ex) {
				throw new ConsistencyErrorException("Add manager to newly created VO failed because there is a particular manager already assigned", ex);
			}
		} else {
			log.error("Can't set VO manager during creating of the VO. User from perunSession is null. {} {}", vo, sess);
		}

		log.debug("Vo {} created", vo);

		return vo;
	}

	@Override
	public Vo updateVo(PerunSession sess, Vo vo) throws InternalErrorException {
		getPerunBl().getAuditer().log(sess, new VoUpdated(vo));
		return getVosManagerImpl().updateVo(sess, vo);
	}

	@Override
	public Vo getVoByShortName(PerunSession sess, String shortName) throws InternalErrorException, VoNotExistsException {
		return getVosManagerImpl().getVoByShortName(sess, shortName);
	}

	@Override
	public Vo getVoById(PerunSession sess, int id) throws InternalErrorException, VoNotExistsException {
		return getVosManagerImpl().getVoById(sess, id);
	}

	@Override
	public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString, int maxNumOfResults) throws InternalErrorException {
		List<ExtSource> extSources = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);
		return this.findCandidates(sess, vo, searchString, maxNumOfResults, extSources, true);
	}

	public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString, int maxNumOfResults, List<ExtSource> extSources, boolean filterExistingMembers) throws InternalErrorException {
		List<Candidate> candidates = new ArrayList<>();
		int numOfResults = 0;

		try {
			// Iterate through given extSources
			for (ExtSource source : extSources) {
				try {
					// Info if this is only simple ext source, change behavior if not
					boolean simpleExtSource = true;

					// Get potential subjects from the extSource
					List<Map<String, String>> subjects;
					try {
						if (source instanceof ExtSourceApi) {
							// find subjects with all their properties
							subjects = ((ExtSourceApi) source).findSubjects(searchString, maxNumOfResults);
							simpleExtSource = false;
						} else {
							// find subjects only with logins - they then must be retrieved by login
							subjects = ((ExtSourceSimpleApi) source).findSubjectsLogins(searchString, maxNumOfResults);
						}
					} catch (ExtSourceUnsupportedOperationException e1) {
						log.warn("ExtSource {} doesn't support findSubjects", source.getName());
						continue;
					} catch (InternalErrorException e) {
						log.error("Error occurred on ExtSource {},  Exception {}.", source.getName(), e);
						continue;
					} finally {
						try {
							((ExtSourceSimpleApi) source).close();
						} catch (ExtSourceUnsupportedOperationException e) {
							// ExtSource doesn't support that functionality, so silently skip it.
						} catch (InternalErrorException e) {
							log.error("Can't close extSource connection. Cause: {}", e);
						}
					}

					Set<String> uniqueLogins = new HashSet<>();
					for (Map<String, String> s : subjects) {
						// Check if the user has unique identifier within extSource
						if ((s.get("login") == null) || (s.get("login") != null && s.get("login").isEmpty())) {
							log.error("User '{}' cannot be added, because he/she doesn't have a unique identifier (login)", s);
							// Skip to another user
							continue;
						}

						String extLogin = s.get("login");

						// check uniqueness of every login in extSource
						if (uniqueLogins.contains(extLogin)) {
							throw new InternalErrorException("There are more than 1 login '" + extLogin + "' getting from extSource '" + source + "'");
						} else {
							uniqueLogins.add(extLogin);
						}

						// Get Candidate
						Candidate candidate;
						try {
							if (simpleExtSource) {
								// retrieve data about subjects from ext source based on ext. login
								candidate = getPerunBl().getExtSourcesManagerBl().getCandidate(sess, source, extLogin);
							} else {
								// retrieve data about subjects from subjects we already have locally
								candidate = getPerunBl().getExtSourcesManagerBl().getCandidate(sess, s, source, extLogin);
							}
						} catch (ExtSourceNotExistsException e) {
							throw new ConsistencyErrorException("Getting candidate from non-existing extSource " + source, e);
						} catch (CandidateNotExistsException e) {
							throw new ConsistencyErrorException("findSubjects returned that candidate, but getCandidate cannot find him using login " + extLogin, e);
						} catch (ExtSourceUnsupportedOperationException e) {
							throw new InternalErrorException("extSource supports findSubjects but not getCandidate???", e);
						}

						if (filterExistingMembers) {
							try {
								getPerunBl().getMembersManagerBl().getMemberByUserExtSources(sess, vo, candidate.getUserExtSources());
								// Candidate is already a member of the VO, so do not add him to the list of candidates
								continue;
							} catch (MemberNotExistsException e) {
								// This is OK
							}
						}

						// Add candidate to the list of candidates
						log.debug("findCandidates: returning candidate: {}", candidate);
						candidates.add(candidate);

						numOfResults++;
						// Stop getting new members if the number of already retrieved members exceeded the maxNumOfResults
						if (maxNumOfResults > 0 && numOfResults >= maxNumOfResults) {
							break;
						}
					}

				} catch (InternalErrorException e) {
					log.error("Failed to get candidates from ExtSource: {}", source);
				}
				// Stop walking through next sources if the number of already retrieved members exceeded the maxNumOfResults
				if (maxNumOfResults > 0 && numOfResults >= maxNumOfResults) {
					break;
				}
			}

			log.debug("Returning {} potential members for vo {}", candidates.size(), vo);
			return candidates;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString) throws InternalErrorException {
		return this.findCandidates(sess, vo, searchString, 0);
	}

	@Override
	public List<Candidate> findCandidates(PerunSession sess, Group group, String searchString) throws InternalErrorException {
		List<ExtSource> extSources = getPerunBl().getExtSourcesManagerBl().getGroupExtSources(sess, group);
		return this.findCandidates(sess, group, searchString, extSources, true);
	}

	public List<Candidate> findCandidates(PerunSession sess, Group group, String searchString, List<ExtSource> extSources, boolean filterExistingMembers) throws InternalErrorException {
		List<Candidate> candidates = new ArrayList<>();

		try {
			// Iterate through given extSources
			for (ExtSource source : extSources) {
				try {
					// Info if this is only simple ext source, change behavior if not
					boolean simpleExtSource = true;

					// Get potential subjects from the extSource
					List<Map<String, String>> subjects;
					try {
						if (source instanceof ExtSourceApi) {
							// find subjects with all their properties
							subjects = ((ExtSourceApi) source).findSubjects(searchString);
							simpleExtSource = false;
						} else {
							// find subjects only with logins - they then must be retrieved by login
							subjects = ((ExtSourceSimpleApi) source).findSubjectsLogins(searchString);
						}
					} catch (ExtSourceUnsupportedOperationException e1) {
						log.warn("ExtSource {} doesn't support findSubjects", source.getName());
						continue;
					} catch (InternalErrorException e) {
						log.error("Error occurred on ExtSource {},  Exception {}.", source.getName(), e);
						continue;
					} finally {
						try {
							((ExtSourceSimpleApi) source).close();
						} catch (ExtSourceUnsupportedOperationException e) {
							// ExtSource doesn't support that functionality, so silently skip it.
						} catch (InternalErrorException e) {
							log.error("Can't close extSource connection. Cause: {}", e);
						}
					}

					Set<String> uniqueLogins = new HashSet<>();
					for (Map<String, String> s : subjects) {
						// Check if the user has unique identifier within extSource
						if ((s.get("login") == null) || (s.get("login") != null && s.get("login").isEmpty())) {
							log.error("User '{}' cannot be added, because he/she doesn't have a unique identifier (login)", s);
							// Skip to another user
							continue;
						}

						String extLogin = s.get("login");

						// check uniqueness of every login in extSource
						if (uniqueLogins.contains(extLogin)) {
							throw new InternalErrorException("There are more than 1 login '" + extLogin + "' getting from extSource '" + source + "'");
						} else {
							uniqueLogins.add(extLogin);
						}

						// Get Candidate
						Candidate candidate;
						try {
							if (simpleExtSource) {
								// retrieve data about subjects from ext source based on ext. login
								candidate = getPerunBl().getExtSourcesManagerBl().getCandidate(sess, source, extLogin);
							} else {
								// retrieve data about subjects from subjects we already have locally
								candidate = getPerunBl().getExtSourcesManagerBl().getCandidate(sess, s, source, extLogin);
							}
						} catch (ExtSourceNotExistsException e) {
							throw new ConsistencyErrorException("Getting candidate from non-existing extSource " + source, e);
						} catch (CandidateNotExistsException e) {
							throw new ConsistencyErrorException("findSubjects returned that candidate, but getCandidate cannot find him using login " + extLogin, e);
						} catch (ExtSourceUnsupportedOperationException e) {
							throw new InternalErrorException("extSource supports findSubjects but not getCandidate???", e);
						}

						if (filterExistingMembers) {
							try {
								Vo vo = getPerunBl().getVosManagerBl().getVoById(sess, group.getVoId());
								getPerunBl().getMembersManagerBl().getMemberByUserExtSources(sess, vo, candidate.getUserExtSources());
								// Candidate is already a member of the VO, so do not add him to the list of candidates
								continue;
							} catch (VoNotExistsException e) {
								throw new InternalErrorException(e);
							} catch (MemberNotExistsException e) {
								// This is OK
							}
						}

						// Add candidate to the list of candidates
						log.debug("findCandidates: returning candidate: {}", candidate);
						candidates.add(candidate);

					}
				} catch (InternalErrorException e) {
					log.error("Failed to get candidates from ExtSource: {}", source);
				}
			}

			log.debug("Returning {} potential members for group {}", candidates.size(), group);
			return candidates;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, List<String> attrNames, String searchString) throws InternalErrorException {
		List<ExtSource> extSources = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);
		List<RichUser> richUsers = getRichUsersForMemberCandidates(sess, attrNames, searchString);
		List<Candidate> candidates = findCandidates(sess, vo, searchString, 0, extSources, false);

		return createMemberCandidates(sess, richUsers, vo, candidates, attrNames);
	}

	@Override
	public List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, Group group, List<String> attrNames, String searchString, List<ExtSource> extSources) throws InternalErrorException {
		List<RichUser> richUsers = getRichUsersForMemberCandidates(sess, vo, attrNames, searchString);
		List<Candidate> candidates = findCandidates(sess, group, searchString, extSources, false);

		if (vo == null) {
			vo = getPerunBl().getGroupsManagerBl().getVo(sess, group);
		}

		return createMemberCandidates(sess, richUsers, vo, group, candidates, attrNames);
	}

	/**
	 * <p>Finds RichUsers who matches the given search string. Users are searched in the whole Perun.</p>
	 * <p>The RichUsers are returned with attributes of given names.</p>
	 *
	 * @param sess session
	 * @param attrNames names of attributes that will be returned
	 * @param searchString string used to find users
	 * @return List of RichUsers from whole Perun, who matches the given String
	 * @throws InternalErrorException internal error
	 */
	private List<RichUser> getRichUsersForMemberCandidates(PerunSession sess, List<String> attrNames, String searchString) throws InternalErrorException {
		return getRichUsersForMemberCandidates(sess, null, attrNames, searchString);
	}

	/**
	 * <p>Finds RichUsers who matches the given search string. If the given Vo is null,
	 * they are searched in the whole Perun. If th Vo is not null, then are returned
	 * only RichUsers who has a member inside this Vo.</p>
	 * <p>The RichUsers are returned with attributes of given names.</p>
	 *
	 *
	 * @param sess session
	 * @param vo virtual organization, users are searched only inside this vo; if is null, then in the whole Perun
	 * @param attrNames names of attributes that will be returned
	 * @param searchString string used to find users
	 * @return List of RichUsers inside given Vo, or in whole perun, who matches the given String
	 * @throws InternalErrorException internal error
	 */
	private List<RichUser> getRichUsersForMemberCandidates(PerunSession sess, Vo vo, List<String> attrNames, String searchString) throws InternalErrorException {
		List<RichUser> richUsers;

		if (vo != null) {
			List<Member> voMembers = getPerunBl().getMembersManagerBl().findMembersInVo(sess, vo, searchString);
			List<User> voUsers = new ArrayList<>();
			for (Member member : voMembers) {
				voUsers.add(getPerunBl().getUsersManagerBl().getUserByMember(sess, member));
			}

			richUsers = getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributesByNames(sess, voUsers, attrNames);
		} else {
			try {
				richUsers = getPerunBl().getUsersManagerBl().findRichUsersWithAttributes(sess, searchString, attrNames);
			} catch (UserNotExistsException e) {
				richUsers = new ArrayList<>();
			}

		}
		return richUsers;
	}

	@Override
	public void addAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException {
		List<User> adminsOfVo = this.getAdmins(sess, vo);
		if (adminsOfVo.contains(user)) throw new AlreadyAdminException(user, vo);
		AuthzResolverBlImpl.setRole(sess, user, vo, Role.VOADMIN);
		log.debug("User [{}] added like administrator to VO [{}]", user, vo);
	}

	@Override
	public void addAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException {
		List<Group> adminsOfVo = this.getAdminGroups(sess, vo);
		if (adminsOfVo.contains(group)) throw new AlreadyAdminException(group, vo);
		AuthzResolverBlImpl.setRole(sess, group, vo, Role.VOADMIN);
		log.debug("Group [{}] added like administrator to VO [{}]", group, vo);
	}

	@Override
	public void removeAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException {
		List<User> adminsOfVo = this.getAdmins(sess, vo);
		if (!adminsOfVo.contains(user)) throw new UserNotAdminException(user);
		AuthzResolverBlImpl.unsetRole(sess, user, vo, Role.VOADMIN);
		log.debug("User [{}] deleted like administrator from VO [{}]", user, vo);
	}

	@Override
	public void removeAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException {
		List<Group> adminsOfVo = this.getAdminGroups(sess, vo);
		if (!adminsOfVo.contains(group)) throw new GroupNotAdminException(group);
		AuthzResolverBlImpl.unsetRole(sess, group, vo, Role.VOADMIN);
		log.debug("Group [{}] deleted like administrator from VO [{}]", group, vo);
	}

	@Override
	public List<User> getAdmins(PerunSession perunSession, Vo vo, Role role, boolean onlyDirectAdmins) throws InternalErrorException {
		if (onlyDirectAdmins) {
			return getVosManagerImpl().getDirectAdmins(perunSession, vo, role);
		} else {
			return getVosManagerImpl().getAdmins(perunSession, vo, role);
		}
	}

	@Override
	public List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo, Role role, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, vo, role, onlyDirectAdmins);
		List<RichUser> richUsers;

		if (allUserAttributes) {
			richUsers = perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
		} else {
			try {
				richUsers = getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
			} catch (AttributeNotExistsException ex) {
				throw new InternalErrorException("One of Attribute not exist.", ex);
			}
		}
		return richUsers;
	}

	@Override
	public List<Group> getAdminGroups(PerunSession perunSession, Vo vo, Role role) throws InternalErrorException {
		return getVosManagerImpl().getAdminGroups(perunSession, vo, role);
	}

	@Override
	@Deprecated
	public List<User> getAdmins(PerunSession sess, Vo vo) throws InternalErrorException {
		return getVosManagerImpl().getAdmins(sess, vo);
	}

	@Deprecated
	@Override
	public List<User> getDirectAdmins(PerunSession sess, Vo vo) throws InternalErrorException {
		return getVosManagerImpl().getDirectAdmins(sess, vo);
	}

	@Deprecated
	@Override
	public List<RichUser> getDirectRichAdmins(PerunSession sess, Vo vo) throws InternalErrorException, UserNotExistsException {
		return perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(sess, getVosManagerImpl().getDirectAdmins(sess, vo));
	}

	@Deprecated
	@Override
	public List<Group> getAdminGroups(PerunSession sess, Vo vo) throws InternalErrorException {
		return getVosManagerImpl().getAdminGroups(sess, vo);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, vo);
		return perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Vo vo) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, vo);
		return perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getRichAdmins(perunSession, vo), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	@Override
	@Deprecated
	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getDirectRichAdmins(perunSession, vo), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	@Override
	public void checkVoExists(PerunSession sess, Vo vo) throws InternalErrorException, VoNotExistsException {
		getVosManagerImpl().checkVoExists(sess, vo);
	}

	@Override
	public List<Vo> getVosByPerunBean(PerunSession sess, PerunBean perunBean) throws InternalErrorException, VoNotExistsException {
		List<Vo> vos = new ArrayList<>();

		//All possible useful objects
		Vo vo = null;
		Facility facility = null;
		Group group = null;
		Member member = null;
		User user = null;
		Host host = null;
		Resource resource = null;

		if (perunBean != null) {
			if (perunBean instanceof Vo) vo = (Vo) perunBean;
			else if (perunBean instanceof Facility) facility = (Facility) perunBean;
			else if (perunBean instanceof Group) group = (Group) perunBean;
			else if (perunBean instanceof Member) member = (Member) perunBean;
			else if (perunBean instanceof User) user = (User) perunBean;
			else if (perunBean instanceof Host) host = (Host) perunBean;
			else if (perunBean instanceof Resource) resource = (Resource) perunBean;
			else {
				throw new InternalErrorException("There is unrecognized object in primaryHolder of aidingAttr.");
			}
		} else {
			throw new InternalErrorException("Aiding attribtue must have primaryHolder which is not null.");
		}

		//Important For Groups not work with Subgroups! Invalid members are executed too.

		if (group != null) {
			vos.add(getPerunBl().getVosManagerBl().getVoById(sess, group.getVoId()));
		} else if (member != null) {
			vos.add(getPerunBl().getMembersManagerBl().getMemberVo(sess, member));
		} else if (resource != null) {
			vos.add(getPerunBl().getVosManagerBl().getVoById(sess, resource.getVoId()));
		} else if (user != null) {
			vos.addAll(getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user));
		} else if (host != null) {
			facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
			vos.addAll(getPerunBl().getFacilitiesManagerBl().getAllowedVos(sess, facility));
		} else if (facility != null) {
			vos.addAll(getPerunBl().getFacilitiesManagerBl().getAllowedVos(sess, facility));
		} else {
			vos.add(vo);
		}

		vos = new ArrayList<>(new HashSet<>(vos));
		return vos;
	}

	@Override
	public int getVosCount(PerunSession sess) throws InternalErrorException {
		return getVosManagerImpl().getVosCount(sess);
	}

	@Override
	public boolean isUserInRoleForVo(PerunSession session, User user, Role role, Vo vo, boolean checkGroups) throws InternalErrorException {
		if (AuthzResolverBlImpl.isUserInRoleForVo(session, user, role, vo)) return true;
		if (checkGroups) {
			List<Member> members = getPerunBl().getMembersManagerBl().getMembersByUser(session, user);
			List<Group> allGroups = new ArrayList<>();
			for (Member member : members) {
				allGroups.addAll(getPerunBl().getGroupsManagerBl().getAllMemberGroups(session, member));
			}
			for (Group group : allGroups) {
				if (AuthzResolverBlImpl.isGroupInRoleForVo(session, group, role, vo)) return true;
			}
		}
		return false;
	}

	@Override
	public void handleUserLostVoRole(PerunSession sess, User user, Vo vo, Role role) throws InternalErrorException {
		log.debug("handleUserLostVoRole(user={},vo={},role={})",user.getLastName(),vo.getShortName(),role);
		switch (role) {
			case SPONSOR:
				removeSponsorFromSponsoredMembers(sess, vo, user);
				break;
		}
	}

	@Override
	public void handleGroupLostVoRole(PerunSession sess, Group group, Vo vo, Role role) throws InternalErrorException {
		switch (role) {
			case SPONSOR:
				//remove all group members as sponsors
				UsersManagerBl um = getPerunBl().getUsersManagerBl();
				for (Member groupMember : getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group)) {
					removeSponsorFromSponsoredMembers(sess, vo, um.getUserByMember(sess, groupMember));
				}
				break;
		}
	}

	private void removeSponsorFromSponsoredMembers(PerunSession sess, Vo vo, User user) throws InternalErrorException {
		log.debug("removeSponsorFromSponsoredMembers(vo={},user={})",vo.getShortName(),user.getLastName());
		MembersManagerBl membersManagerBl = getPerunBl().getMembersManagerBl();
		for (Member sponsoredMember : membersManagerBl.getSponsoredMembers(sess, vo, user)) {
			log.debug("removing sponsor from sponsored member {}",sponsoredMember.getId());
			membersManagerBl.removeSponsor(sess, sponsoredMember, user);
		}
	}

	/**
	 * Creates MemberCandidates for given RichUsers, group and candidates. If the given group is not null
	 * then to all members who are in this group is assigned the sourceGroupId of the given group.
	 * The given group can be null.
	 *
	 * @param sess session
	 * @param users users
	 * @param candidates candidates
	 * @return list of MemberCandidates for given RichUsers, group and candidates
	 * @throws InternalErrorException internal error
	 */
	private List<MemberCandidate> createMemberCandidates(PerunSession sess, List<RichUser> users, Vo vo, List<Candidate> candidates, List<String> attrNames) throws InternalErrorException {
		return createMemberCandidates(sess, users, vo, null, candidates, attrNames);
	}

	/**
	 * Creates MemberCandidates for given RichUsers, vo, group and candidates. If the given group is not null
	 * then to all members who are in this group is assigned the sourceGroupId of the given group.
	 * The given group can be null.
	 *
	 * @param sess session
	 * @param users users
	 * @param group group
	 * @param candidates candidates
	 * @return list of MemberCandidates for given RichUsers, group and candidates
	 * @throws InternalErrorException internal error
	 */
	private List<MemberCandidate> createMemberCandidates(PerunSession sess, List<RichUser> users, Vo vo, Group group, List<Candidate> candidates, List<String> attrNames) throws InternalErrorException {
		List<MemberCandidate> memberCandidates = new ArrayList<>();

		// try to find matching RichUser for candidates
		for (Candidate candidate : candidates) {
			MemberCandidate mc = new MemberCandidate();
			mc.setCandidate(candidate);

			try {
				User user = getPerunBl().getUsersManagerBl().getUserByUserExtSources(sess, candidate.getUserExtSources());
				RichUser richUser = getPerunBl().getUsersManagerBl().convertUserToRichUserWithAttributesByNames(sess, user, attrNames);

				mc.setRichUser(richUser);
			} catch (UserNotExistsException ignored) {
				// no matching user was found
			}

			memberCandidates.add(mc);
		}

		List<RichUser> foundRichUsers = memberCandidates.stream()
				.map(MemberCandidate::getRichUser)
				.collect(Collectors.toList());

		// create MemberCandidates for RichUsers without candidate
		for (RichUser richUser : users) {
			if (!foundRichUsers.contains(richUser)) {
				MemberCandidate mc = new MemberCandidate();
				mc.setRichUser(richUser);
				memberCandidates.add(mc);
			}
		}

		// try to find member for MemberCandidates with not null RichUser
		for (MemberCandidate memberCandidate : memberCandidates) {
			if (memberCandidate.getRichUser() != null) {
				try {
					Member member = getPerunBl().getMembersManagerBl().getMemberByUser(sess, vo, memberCandidate.getRichUser());

					if (group != null) {
						// check if member is in group
						if (getPerunBl().getGroupsManagerBl().isGroupMember(sess, group, member)) {
							member.setSourceGroupId(group.getId());
						}
					}
					memberCandidate.setMember(member);
				} catch (MemberNotExistsException ignored) {
					// no matching member was found
				}
			}
		}

		return memberCandidates;
	}


	/**
	 * Gets the vosManagerImpl.
	 *
	 * @return The vosManagerImpl.
	 */
	private VosManagerImplApi getVosManagerImpl() {
		return this.vosManagerImpl;
	}

	/**
	 * Sets the perunBl for this instance.
	 *
	 * @param perunBl The perunBl.
	 */
	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}

	/**
	 * Gets the perunBl.
	 *
	 * @return The perunBl.
	 */
	public PerunBl getPerunBl() {
		return this.perunBl;
	}


}
