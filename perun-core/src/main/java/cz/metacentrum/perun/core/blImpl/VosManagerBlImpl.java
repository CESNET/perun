package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberSuspended;
import cz.metacentrum.perun.audit.events.MembersManagerEvents.MemberUnsuspended;
import cz.metacentrum.perun.audit.events.VoManagerEvents.VoCreated;
import cz.metacentrum.perun.audit.events.VoManagerEvents.VoDeleted;
import cz.metacentrum.perun.audit.events.VoManagerEvents.VoUpdated;
import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberCandidate;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadySponsorException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotSponsoredException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotInRoleException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.MembersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import cz.metacentrum.perun.core.implApi.ExtSourceSimpleApi;
import cz.metacentrum.perun.core.implApi.VosManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
	private final static Date FAR_FUTURE = new GregorianCalendar(2999, Calendar.JANUARY, 1).getTime();

	private final VosManagerImplApi vosManagerImpl;
	private PerunBl perunBl;

	/**
	 * Constructor.
	 */
	public VosManagerBlImpl(VosManagerImplApi vosManagerImpl) {
		this.vosManagerImpl = vosManagerImpl;
	}

	@Override
	public List<Vo> getVos(PerunSession sess) {
		return getVosManagerImpl().getVos(sess);
	}

	@Override
	public void deleteVo(PerunSession sess, Vo vo, boolean forceDelete) {
		log.debug("Deleting vo {}", vo);

		try {
			//remove admins of this vo
			List<Group> adminGroups = getVosManagerImpl().getAdminGroups(sess, vo);

			for (Group adminGroup : adminGroups) {
				try {
					AuthzResolverBlImpl.unsetRole(sess, adminGroup, vo, Role.VOADMIN);
				} catch (GroupNotAdminException e) {
					log.warn("When trying to unsetRole VoAdmin for group {} in the vo {} the exception was thrown {}", adminGroup, vo, e);
					//skip and log as warning
				}
			}

			List<User> adminUsers = getVosManagerImpl().getAdmins(sess, vo);

			for (User adminUser : adminUsers) {
				try {
					AuthzResolverBlImpl.unsetRole(sess, adminUser, vo, Role.VOADMIN);
				} catch (UserNotAdminException e) {
					log.warn("When trying to unsetRole VoAdmin for user {} in the vo {} the exception was thrown {}", adminUser, vo, e);
					//skip and log as warning
				}
			}

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
		Vo deletedVo = getVosManagerImpl().deleteVo(sess, vo);
		getPerunBl().getAuditer().log(sess, new VoDeleted(deletedVo));
	}

	@Override
	public void deleteVo(PerunSession sess, Vo vo) {
		// delete VO only if it is completely empty
		this.deleteVo(sess, vo, false);
	}

	@Override
	public Vo createVo(PerunSession sess, Vo vo) throws VoExistsException {
		// Create entries in the DB and Grouper
		vo = getVosManagerImpl().createVo(sess, vo);
		getPerunBl().getAuditer().log(sess, new VoCreated(vo));

		User user = sess.getPerunPrincipal().getUser();
		//set creator as VO manager
		if (user != null) {
			try {
				AuthzResolverBlImpl.setRole(sess, user, vo, Role.VOADMIN);
				log.debug("User {} added like administrator to VO {}", user, vo);
			} catch (AlreadyAdminException ex) {
				throw new ConsistencyErrorException("Add manager to newly created VO failed because there is a particular manager already assigned", ex);
			} catch (RoleCannotBeManagedException e) {
				throw new InternalErrorException(e);
			}
		} else {
			log.error("Can't set VO manager during creating of the VO. User from perunSession is null. {} {}", vo, sess);
		}

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

		log.info("Vo {} created", vo);

		return vo;
	}

	@Override
	public Vo updateVo(PerunSession sess, Vo vo) {
		getPerunBl().getAuditer().log(sess, new VoUpdated(vo));
		return getVosManagerImpl().updateVo(sess, vo);
	}

	@Override
	public Vo getVoByShortName(PerunSession sess, String shortName) throws VoNotExistsException {
		return getVosManagerImpl().getVoByShortName(sess, shortName);
	}

	@Override
	public Vo getVoById(PerunSession sess, int id) throws VoNotExistsException {
		return getVosManagerImpl().getVoById(sess, id);
	}

	@Override
	public List<Vo> getVosByIds(PerunSession sess, List<Integer> ids) {
		return getVosManagerImpl().getVosByIds(sess, ids);
	}

	@Override
	public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString, int maxNumOfResults) {
		List<ExtSource> extSources = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);
		return this.findCandidates(sess, vo, searchString, maxNumOfResults, extSources, true);
	}

	public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString, int maxNumOfResults, List<ExtSource> extSources, boolean filterExistingMembers) {
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
							log.error("Can't close extSource connection.", e);
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
								candidate = new Candidate(getPerunBl().getExtSourcesManagerBl().getCandidate(sess, source, extLogin));
							} else {
								// retrieve data about subjects from subjects we already have locally
								candidate = new Candidate(getPerunBl().getExtSourcesManagerBl().getCandidate(sess, s, source, extLogin));
							}
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
	public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString) {
		return this.findCandidates(sess, vo, searchString, 0);
	}

	@Override
	public List<Candidate> findCandidates(PerunSession sess, Group group, String searchString) {
		List<ExtSource> extSources = getPerunBl().getExtSourcesManagerBl().getGroupExtSources(sess, group);
		return this.findCandidates(sess, group, searchString, extSources, true);
	}

	public List<Candidate> findCandidates(PerunSession sess, Group group, String searchString, List<ExtSource> extSources, boolean filterExistingMembers) {
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
							log.error("Can't close extSource connection.", e);
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
								candidate = new Candidate(getPerunBl().getExtSourcesManagerBl().getCandidate(sess, source, extLogin));
							} else {
								// retrieve data about subjects from subjects we already have locally
								candidate = new Candidate(getPerunBl().getExtSourcesManagerBl().getCandidate(sess, s, source, extLogin));
							}
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
	public List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, List<String> attrNames, String searchString) {
		List<ExtSource> extSources = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);
		List<RichUser> richUsers = getRichUsersForMemberCandidates(sess, attrNames, searchString);
		List<Candidate> candidates = findCandidates(sess, vo, searchString, 0, extSources, false);

		return createMemberCandidates(sess, richUsers, vo, candidates, attrNames);
	}

	@Override
	public List<MemberCandidate> getCompleteCandidates(PerunSession sess, Vo vo, Group group, List<String> attrNames, String searchString, List<ExtSource> extSources) {
		List<RichUser> richUsers = getRichUsersForMemberCandidates(sess, vo, attrNames, searchString, extSources);
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
	private List<RichUser> getRichUsersForMemberCandidates(PerunSession sess, List<String> attrNames, String searchString) {
		return getRichUsersForMemberCandidates(sess, null, attrNames, searchString, null);
	}

	/**
	 * <p>Finds RichUsers who matches the given search string. If the given Vo is null,
	 * they are searched in the whole Perun. If th Vo is not null, then are returned
	 * only RichUsers who has a member inside this Vo or who has ues in any of given ExtSources.</p>
	 * <p>The RichUsers are returned with attributes of given names.</p>
	 *
	 *
	 * @param sess session
	 * @param vo virtual organization, users are searched inside this vo; if is null, then in the whole Perun
	 * @param attrNames names of attributes that will be returned
	 * @param searchString string used to find users
	 * @param extSources list of extSources to possibly search users with ues in these extSources
	 * @return List of RichUsers inside given Vo, or in whole perun, who matches the given String
	 * @throws InternalErrorException internal error
	 */
	private List<RichUser> getRichUsersForMemberCandidates(PerunSession sess, Vo vo, List<String> attrNames, String searchString, List<ExtSource> extSources) {
		List<RichUser> richUsers;

		if (vo != null) {
			try {
				List<RichUser> allRichUsers = getPerunBl().getUsersManagerBl().findRichUsersWithAttributes(sess, searchString, attrNames);
				richUsers = new ArrayList<>();

				// filter users who don't have ues in any of the extSources nor they are in given vo
				for (RichUser richUser : allRichUsers) {
					boolean extSourceMatch = getPerunBl().getUsersManagerBl().getUserExtSources(sess, richUser).stream()
						.map(UserExtSource::getExtSource)
						.anyMatch(extSources::contains);
					if (extSourceMatch) {
						richUsers.add(richUser);
					} else {
						try {
							Member member = getPerunBl().getMembersManagerBl().getMemberByUser(sess, vo, richUser);
							richUsers.add(richUser);
						} catch (MemberNotExistsException e) {
							// richUser is not in vo nor he has ues in any of given ExtSources, skip him
						}
					}
				}
			} catch (UserNotExistsException e) {
				richUsers = new ArrayList<>();
			}
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
	public List<User> getAdmins(PerunSession perunSession, Vo vo, String role, boolean onlyDirectAdmins) {
		if (onlyDirectAdmins) {
			return getVosManagerImpl().getDirectAdmins(perunSession, vo, role);
		} else {
			return getVosManagerImpl().getAdmins(perunSession, vo, role);
		}
	}

	@Override
	public List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo, String role, List<String> specificAttributes, boolean allUserAttributes, boolean onlyDirectAdmins) throws UserNotExistsException {
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
	public List<Group> getAdminGroups(PerunSession perunSession, Vo vo, String role) {
		return getVosManagerImpl().getAdminGroups(perunSession, vo, role);
	}

	@Override
	@Deprecated
	public List<User> getAdmins(PerunSession sess, Vo vo) {
		return getVosManagerImpl().getAdmins(sess, vo);
	}

	@Deprecated
	@Override
	public List<User> getDirectAdmins(PerunSession sess, Vo vo) {
		return getVosManagerImpl().getDirectAdmins(sess, vo);
	}

	@Deprecated
	@Override
	public List<RichUser> getDirectRichAdmins(PerunSession sess, Vo vo) {
		return perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(sess, getVosManagerImpl().getDirectAdmins(sess, vo));
	}

	@Deprecated
	@Override
	public List<Group> getAdminGroups(PerunSession sess, Vo vo) {
		return getVosManagerImpl().getAdminGroups(sess, vo);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo) {
		List<User> users = this.getAdmins(perunSession, vo);
		return perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Vo vo) throws UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, vo);
		return perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
	}

	@Override
	@Deprecated
	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes) {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getRichAdmins(perunSession, vo), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	@Override
	@Deprecated
	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes) {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getDirectRichAdmins(perunSession, vo), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	@Override
	public void checkVoExists(PerunSession sess, Vo vo) throws VoNotExistsException {
		getVosManagerImpl().checkVoExists(sess, vo);
	}

	@Override
	public List<Vo> getVosByPerunBean(PerunSession sess, Group group) throws VoNotExistsException {
		return Collections.singletonList(getPerunBl().getVosManagerBl().getVoById(sess, group.getVoId()));
	}

	@Override
	public List<Vo> getVosByPerunBean(PerunSession sess, Member member) {
		return Collections.singletonList(getPerunBl().getMembersManagerBl().getMemberVo(sess, member));
	}

	@Override
	public List<Vo> getVosByPerunBean(PerunSession sess, Resource resource) throws VoNotExistsException {
		return Collections.singletonList(getPerunBl().getVosManagerBl().getVoById(sess, resource.getVoId()));
	}

	@Override
	public List<Vo> getVosByPerunBean(PerunSession sess, User user) {
		return new ArrayList<>(new HashSet<>(getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user)));
	}

	@Override
	public List<Vo> getVosByPerunBean(PerunSession sess, Host host) {
		Facility facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
		return new ArrayList<>(new HashSet<>(getPerunBl().getFacilitiesManagerBl().getAllowedVos(sess, facility)));
	}

	@Override
	public List<Vo> getVosByPerunBean(PerunSession sess, Facility facility) {
		return new ArrayList<>(new HashSet<>(getPerunBl().getFacilitiesManagerBl().getAllowedVos(sess, facility)));
	}

	@Override
	public int getVosCount(PerunSession sess) {
		return getVosManagerImpl().getVosCount(sess);
	}

	@Override
	public boolean isUserInRoleForVo(PerunSession session, User user, String role, Vo vo, boolean checkGroups) {
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
	public void handleUserLostVoRole(PerunSession sess, User user, Vo vo, String role) {
		log.debug("handleUserLostVoRole(user={},vo={},role={})",user.getLastName(),vo.getShortName(),role);
		switch (role) {
			case Role.SPONSOR:
				removeSponsorFromSponsoredMembers(sess, vo, user);
				break;
		}
	}

	@Override
	public void handleGroupLostVoRole(PerunSession sess, Group group, Vo vo, String role) {
		switch (role) {
			case Role.SPONSOR:
				//remove all group members as sponsors
				UsersManagerBl um = getPerunBl().getUsersManagerBl();
				for (Member groupMember : getPerunBl().getGroupsManagerBl().getGroupMembers(sess, group)) {
					removeSponsorFromSponsoredMembers(sess, vo, um.getUserByMember(sess, groupMember));
				}
				break;
		}
	}

	@Override
	public BanOnVo setBan(PerunSession sess, BanOnVo banOnVo) throws MemberNotExistsException {
		Utils.notNull(banOnVo, "banOnVo");

		Member member = perunBl.getMembersManagerBl().getMemberById(sess, banOnVo.getMemberId());
		banOnVo.setVoId(member.getVoId());

		if (vosManagerImpl.isMemberBanned(sess, member.getId())) {
			return updateBan(sess, banOnVo);
		}

		// if the validity is not specified, set a date from far future
		if (banOnVo.getValidityTo() == null) {
			banOnVo.setValidityTo(FAR_FUTURE);
		}

		banOnVo = vosManagerImpl.setBan(sess, banOnVo);

		// fetch all ban information
		try {
			banOnVo = vosManagerImpl.getBanById(sess, banOnVo.getId());
		} catch (BanNotExistsException e) {
			// shouldn't happen
			throw new ConsistencyErrorException(e);
		}

		perunBl.getAuditer().log(sess, new MemberSuspended(member));

		return banOnVo;
	}

	@Override
	public BanOnVo getBanById(PerunSession sess, int banId) throws BanNotExistsException {
		return vosManagerImpl.getBanById(sess, banId);
	}

	@Override
	public Optional<BanOnVo> getBanForMember(PerunSession sess, int memberId) {
		try {
			return Optional.of(vosManagerImpl.getBanForMember(sess, memberId));
		} catch (BanNotExistsException e) {
			return Optional.empty();
		}
	}

	@Override
	public List<BanOnVo> getBansForVo(PerunSession sess, int voId) {
		return vosManagerImpl.getBansForVo(sess, voId);
	}

	@Override
	public BanOnVo updateBan(PerunSession sess, BanOnVo banOnVo) {
		Utils.notNull(banOnVo, "banOnVo");

		// if the validity is not specified, set a date from far future
		if (banOnVo.getValidityTo() == null) {
			banOnVo.setValidityTo(FAR_FUTURE);
		}

		return vosManagerImpl.updateBan(sess, banOnVo);
	}

	@Override
	public void removeBan(PerunSession sess, int banId) throws BanNotExistsException {
		BanOnVo ban = vosManagerImpl.getBanById(sess, banId);

		vosManagerImpl.removeBan(sess, banId);
		Member member;
		try {
			member = perunBl.getMembersManagerBl().getMemberById(sess, ban.getMemberId());
		} catch (MemberNotExistsException e) {
			// shouldn't happen
			log.error("Failed to find member who was just banned.", e);
			throw new ConsistencyErrorException("Failed to find member who was just banned.", e);
		}

		perunBl.getAuditer().log(sess, new MemberUnsuspended(member));
	}

	@Override
	public void removeBanForMember(PerunSession sess, int memberId) throws BanNotExistsException {
		BanOnVo ban = vosManagerImpl.getBanForMember(sess, memberId);
		removeBan(sess, ban.getId());
	}

	@Override
	public boolean isMemberBanned(PerunSession sess, int memberId) {
		return vosManagerImpl.isMemberBanned(sess, memberId);
	}

	@Override
	public void convertSponsoredUsers(PerunSession sess, Vo vo) {
		perunBl.getUsersManagerBl().getSpecificUsers(sess).stream()
				.filter(User::isSponsoredUser)
				.forEach(user -> convertToSponsoredMember(sess, user, vo));
	}

	@Override
	public void convertSponsoredUsersWithNewSponsor(PerunSession sess, Vo vo, User newSponsor) {
		perunBl.getUsersManagerBl().getSpecificUsers(sess).stream()
				.filter(User::isSponsoredUser)
				.forEach(user -> convertToSponsoredMemberWithNewSponsor(sess, user, newSponsor, vo));
	}

	/**
	 * Sponsor given user by the given newSponsor in the given vo. If the newSponsor doesn't have
	 * the SPONSOR role, it will be set to him.
	 *
	 * @param sess session
	 * @param user user to be sponsored
	 * @param newSponsor new sponsor
	 * @param vo vo where the given user will be sponsored
	 */
	private void convertToSponsoredMemberWithNewSponsor(PerunSession sess, User user, User newSponsor, Vo vo) {
		try {
			Member member = perunBl.getMembersManagerBl().getMemberByUser(sess, vo, user);

			sponsorMemberByUser(sess, member, newSponsor, vo);
		} catch (MemberNotExistsException e) {
			// if the sponsored user is not member of the given vo, skip it
		}
	}

	/**
	 * Converts sponsored user to sponsored member in the given vo.
	 * If the user is not member of the given vo, it is skipped.
	 *
	 * @param sess session
	 * @param user user
	 * @param vo vo where the given user will be sponsored
	 */
	private void convertToSponsoredMember(PerunSession sess, User user, Vo vo) {
		try {
			Member member = perunBl.getMembersManagerBl().getMemberByUser(sess, vo, user);
			List<User> owners = perunBl.getUsersManagerBl().getUsersBySpecificUser(sess, user);

			for (User owner : owners) {
				sponsorMemberByUser(sess, member, owner, vo);
			}
		} catch (MemberNotExistsException e) {
			// if the sponsored user is not member of the given vo, skip it
		}
	}

	/**
	 * Sponsor the given member by the given sponsor in the given vo. If the
	 * member is already sponsored, this method just adds the sponsor to the given member.
	 * If the member is not sponsored at all, it will transform it into a sponsored one
	 * with the given sponsor.
	 *
	 * @param sess session
	 * @param member member to be sponsored
	 * @param sponsor sponsor
	 * @param vo vo where the member is sponsored
	 */
	private void sponsorMemberByUser(PerunSession sess, Member member, User sponsor, Vo vo) {
		try {
			if (!getPerunBl().getVosManagerBl().isUserInRoleForVo(sess, sponsor, Role.SPONSOR, vo, true)) {
				AuthzResolverBlImpl.setRole(sess, sponsor, vo, Role.SPONSOR);
			}

			// we need to refresh information and check if the member is already sponsored
			member = perunBl.getMembersManagerBl().getMemberById(sess, member.getId());

			if (member.isSponsored()) {
				// if the member is already sponsored, just add another sponsor
				perunBl.getMembersManagerBl().sponsorMember(sess, member, sponsor);
			} else {
				// if the member is not sponsored, transform him into a sponsored one
				perunBl.getMembersManagerBl().setSponsorshipForMember(sess, member, sponsor);
			}
		} catch(AlreadySponsorException e) {
			// if the user is already sponsoring the given member, just silently skip
		} catch(PerunException e) {
			throw new InternalErrorException(e);
		}
	}

	private void removeSponsorFromSponsoredMembers(PerunSession sess, Vo vo, User user) {
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
	private List<MemberCandidate> createMemberCandidates(PerunSession sess, List<RichUser> users, Vo vo, List<Candidate> candidates, List<String> attrNames) {
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
	public List<MemberCandidate> createMemberCandidates(PerunSession sess, List<RichUser> users, Vo vo, Group group, List<Candidate> candidates, List<String> attrNames) {
		List<MemberCandidate> memberCandidates = new ArrayList<>();
		Set<Integer> allUsersIds = new HashSet<>();
		int userId;

		// try to find matching RichUser for candidates
		for (Candidate candidate : candidates) {
			MemberCandidate mc = new MemberCandidate();

			try {
				User user = getPerunBl().getUsersManagerBl().getUserByUserExtSources(sess, candidate.getUserExtSources());
				userId = user.getId();

				// check if user already exists in the list
				if(!allUsersIds.contains(userId)) {
					RichUser richUser = getPerunBl().getUsersManagerBl().convertUserToRichUserWithAttributesByNames(sess, user, attrNames);
					mc.setRichUser(richUser);
					memberCandidates.add(mc);
				}
				allUsersIds.add(userId);

			} catch (UserNotExistsException ignored) {
				// no matching user was found
				mc.setCandidate(candidate);
				memberCandidates.add(mc);
			}

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
				Member member = null;
				try {

					member = getPerunBl().getMembersManagerBl().getMemberByUser(sess, vo, memberCandidate.getRichUser());
					if (group != null) {
						member = getPerunBl().getGroupsManagerBl().getGroupMemberById(sess, group, member.getId());
					}

				} catch (MemberNotExistsException ignored) {
					// no matching VO member was found
				} catch (NotGroupMemberException e) {
					// not matching Group member was found
				}

				// put null or matching member
				memberCandidate.setMember(member);

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
