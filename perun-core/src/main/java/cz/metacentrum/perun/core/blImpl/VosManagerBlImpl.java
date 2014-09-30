package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.FacilitiesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.CandidateNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.GroupExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.LoginNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.NotGroupMemberException;
import cz.metacentrum.perun.core.api.exceptions.NotMemberOfParentGroupException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.ExtSourcesManagerImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import cz.metacentrum.perun.core.implApi.VosManagerImplApi;
import java.util.HashSet;

/**
 * VosManager buisness logic
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Slavek Licehammer glory@ics.muni.cz
 */
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

	public List<Vo> getVos(PerunSession sess) throws InternalErrorException {
		return getVosManagerImpl().getVos(sess);
	}

	public void deleteVo(PerunSession sess, Vo vo, boolean forceDelete) throws InternalErrorException, RelationExistsException {
		log.debug("Deleting vo {}", vo);

		try {
			List<Member> members = getPerunBl().getMembersManagerBl().getMembers(sess, vo);

			log.debug("Deleting vo {} members", vo);
			// Check if there are some members left
			if (members != null && members.size() > 0) {
				if (forceDelete) {
					getPerunBl().getMembersManagerBl().deleteAllMembers(sess, vo);
				} else throw new RelationExistsException("Vo vo=" + vo +" contains members");
			}

			log.debug("Removing vo {} resources and theirs atributes", vo);
			// Delete resources
			List<Resource> resources = getPerunBl().getResourcesManagerBl().getResources(sess, vo);
			if ((resources.size() == 0) || ((resources.size() > 0) && forceDelete)) {
				for (Resource resource: resources) {
					getPerunBl().getAttributesManagerBl().removeAllAttributes(sess, resource);
					// Remove binding between service and resource
					List<Service> services = getPerunBl().getResourcesManagerBl().getAssignedServices(sess, resource);
					for (Service service: services) {
						getPerunBl().getResourcesManagerBl().removeService(sess, resource, service);
					}
					getPerunBl().getResourcesManagerBl().deleteResource(sess, resource);
				}
			} else {
				throw new RelationExistsException("Vo vo=" + vo +" contains resources");
			}

			log.debug("Removing vo {} groups", vo);
			// Delete all groups

			List<Group> groups = getPerunBl().getGroupsManagerBl().getGroups(sess, vo);
			if(groups.size() != 1) {
				if(groups.size() < 1) throw new ConsistencyErrorException("'members' group is missing");
				if((groups.size() > 1) && forceDelete) {
					getPerunBl().getGroupsManagerBl().deleteAllGroups(sess, vo);
				} else {
					throw new RelationExistsException("Vo vo=" + vo +" contains groups");
				}
			}

			// Finally delete binding between Vo and external source
			List<ExtSource> ess = getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo);
			log.debug("Deleting {} external sources binded to the vo {}", ess.size(), vo);
			for (ExtSource es: ess) {
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
		getPerunBl().getAuditer().log(sess, "{} deleted.", vo);
	}

	public void deleteVo(PerunSession sess, Vo vo) throws InternalErrorException, RelationExistsException {
		// delete VO only if it is completely empty
		this.deleteVo(sess, vo, false);
	}

	public Vo createVo(PerunSession sess, Vo vo) throws VoExistsException, InternalErrorException {
		// Create entries in the DB and Grouper
		vo = getVosManagerImpl().createVo(sess, vo);
		getPerunBl().getAuditer().log(sess, "{} created.", vo);

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
		if(sess.getPerunPrincipal().getUser() != null) {
			try {
				addAdmin(sess, vo, sess.getPerunPrincipal().getUser());
			} catch(AlreadyAdminException ex) {
				throw new ConsistencyErrorException("Add manager to newly created VO failed because there is particalar manager already assigned", ex);
			}
		} else {
			log.error("Can't set VO manager during creting of the VO. User from perunSession is null. {} {}", vo, sess);
		}

		log.debug("Vo {} created", vo);

		return vo;
	}

	public Vo updateVo(PerunSession sess, Vo vo) throws InternalErrorException {
		getPerunBl().getAuditer().log(sess, "{} updated.", vo);
		return getVosManagerImpl().updateVo(sess, vo);
	}

	public Vo getVoByShortName(PerunSession sess, String shortName) throws InternalErrorException, VoNotExistsException {
		return getVosManagerImpl().getVoByShortName(sess, shortName);
	}

	public Vo getVoById(PerunSession sess, int id) throws InternalErrorException, VoNotExistsException {
		return getVosManagerImpl().getVoById(sess, id);
	}

	public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString, int maxNumOfResults) throws InternalErrorException {
		List<Candidate> candidates = new ArrayList<Candidate>();
		int numOfResults = 0;

		try {
			// Iterate through all registered extSources
			for (ExtSource source : getPerunBl().getExtSourcesManagerBl().getVoExtSources(sess, vo)) {

				// Get potential subjects from the extSource
				List<Map<String, String>> subjects;
				try {
					subjects = ((ExtSourceApi) source).findSubjects(searchString, maxNumOfResults);
				} catch (ExtSourceUnsupportedOperationException e1) {
					log.warn("ExtSource {} doesn't support findSubjects", source.getName());
					continue;
				} catch (InternalErrorException e) {
					log.error("Error occured on ExtSource {},  Exception {}.", source.getName(), e);
					continue;
				} finally {
					try {
						((ExtSourceApi) source).close();
					} catch (ExtSourceUnsupportedOperationException e) {
						// ExtSource doesn't support that functionality, so silentely skip it.
					} catch (InternalErrorException e) {
						log.error("Can't close extSource connection. Cause: {}", e);
					}
				}

				for (Map<String, String> s : subjects) {
					// Check if the user has unique identifier whithin extSource
					if ((s.get("login") == null) || (s.get("login") != null && ((String) s.get("login")).isEmpty())) {
						log.error("User '{}' cannot be added, because he/she doesn't have a unique identifier (login)", s);
						// Skip to another user
						continue;
					}
					String extLogin = (String) s.get("login");

					// Get Canddate
					Candidate candidate;
					try {
						candidate = getPerunBl().getExtSourcesManagerBl().getCandidate(sess, source, extLogin);
					} catch (ExtSourceNotExistsException e) {
						throw new ConsistencyErrorException("Getting candidate from non-existing extSource " + source, e);
					} catch (CandidateNotExistsException e) {
						throw new ConsistencyErrorException("findSubjects returned that candidate, but getCandidate cannot find him using login " + extLogin, e);
					} catch (ExtSourceUnsupportedOperationException e) {
						throw new InternalErrorException("extSource supports findSubjects but not getCandidate???", e);
					}

					try {
						getPerunBl().getMembersManagerBl().getMemberByUserExtSources(sess, vo, candidate.getUserExtSources());
						// Candidate is already a member of the VO, so do not add him to the list of candidates
						continue;
					} catch (MemberNotExistsException e) {
						// This is OK
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

	public List<Candidate> findCandidates(PerunSession sess, Vo vo, String searchString) throws InternalErrorException {
		return this.findCandidates(sess, vo, searchString, 0);
	}

	public void addAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, AlreadyAdminException {
		List<User> adminsOfVo = this.getAdmins(sess, vo);
		if(adminsOfVo.contains(user)) throw new AlreadyAdminException(user, vo);
		AuthzResolverBlImpl.setRole(sess, user, vo, Role.VOADMIN);
		log.debug("User [{}] added like administrator to VO [{}]", user, vo);
	}

	@Override
	public void addAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, AlreadyAdminException {
		List<Group> adminsOfVo = this.getAdminGroups(sess, vo);
		if(adminsOfVo.contains(group)) throw new AlreadyAdminException(group, vo);
		AuthzResolverBlImpl.setRole(sess, group, vo, Role.VOADMIN);
		log.debug("Group [{}] added like administrator to VO [{}]", group, vo);
	}

	public void removeAdmin(PerunSession sess, Vo vo, User user) throws InternalErrorException, UserNotAdminException {
		List<User> adminsOfVo = this.getAdmins(sess, vo);
		if(!adminsOfVo.contains(user)) throw new UserNotAdminException(user);
		AuthzResolverBlImpl.unsetRole(sess, user, vo, Role.VOADMIN);
		log.debug("User [{}] deleted like administrator from VO [{}]", user, vo);
	}

	@Override
	public void removeAdmin(PerunSession sess, Vo vo, Group group) throws InternalErrorException, GroupNotAdminException {
		List<Group> adminsOfVo = this.getAdminGroups(sess, vo);
		if(!adminsOfVo.contains(group)) throw new GroupNotAdminException(group);
		AuthzResolverBlImpl.unsetRole(sess, group, vo, Role.VOADMIN);
		log.debug("Group [{}] deleted like administrator from VO [{}]", group, vo);
	}

	public List<User> getAdmins(PerunSession sess, Vo vo) throws InternalErrorException {
		return getVosManagerImpl().getAdmins(sess, vo);
	}

	@Override
	public List<User> getDirectAdmins(PerunSession sess, Vo vo) throws InternalErrorException {
		return getVosManagerImpl().getDirectAdmins(sess, vo);
	}

	@Override
	public List<RichUser> getDirectRichAdmins(PerunSession sess, Vo vo) throws InternalErrorException, UserNotExistsException {
		return perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(sess, getVosManagerImpl().getDirectAdmins(sess, vo));
	}

	@Override
	public List<Group> getAdminGroups(PerunSession sess, Vo vo) throws InternalErrorException {
		return getVosManagerImpl().getAdminGroups(sess, vo);
	}

	public List<RichUser> getRichAdmins(PerunSession perunSession, Vo vo) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, vo);
		List<RichUser> richUsers = perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
		return richUsers;
	}

	public List<RichUser> getRichAdminsWithAttributes(PerunSession perunSession, Vo vo) throws InternalErrorException, UserNotExistsException {
		List<User> users = this.getAdmins(perunSession, vo);
		List<RichUser> richUsers = perunBl.getUsersManagerBl().getRichUsersFromListOfUsers(perunSession, users);
		List<RichUser> richUsersWithAttributes = perunBl.getUsersManagerBl().getRichUsersWithAttributesFromListOfUsers(perunSession, users);
		return richUsersWithAttributes;
	}

	public List<RichUser> getRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getRichAdmins(perunSession, vo), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	public List<RichUser> getDirectRichAdminsWithSpecificAttributes(PerunSession perunSession, Vo vo, List<String> specificAttributes) throws InternalErrorException, UserNotExistsException {
		try {
			return getPerunBl().getUsersManagerBl().convertUsersToRichUsersWithAttributes(perunSession, this.getDirectRichAdmins(perunSession, vo), getPerunBl().getAttributesManagerBl().getAttributesDefinition(perunSession, specificAttributes));
		} catch (AttributeNotExistsException ex) {
			throw new InternalErrorException("One of Attribute not exist.", ex);
		}
	}

	public void checkVoExists(PerunSession sess, Vo vo) throws InternalErrorException, VoNotExistsException {
		getVosManagerImpl().checkVoExists(sess, vo);
	}

	public List<Vo> getVosByPerunBean(PerunSession sess, PerunBean perunBean) throws InternalErrorException, VoNotExistsException {
		List<Vo> vos = new ArrayList<Vo>();

		//All possible useful objects
		Vo vo = null;
		Facility facility = null;
		Group group = null;
		Member member = null;
		User user = null;
		Host host = null;
		Resource resource = null;

		if(perunBean != null) {
			if(perunBean instanceof Vo) vo = (Vo) perunBean;
			else if(perunBean instanceof Facility) facility = (Facility) perunBean;
			else if(perunBean instanceof Group) group = (Group) perunBean;
			else if(perunBean instanceof Member) member = (Member) perunBean;
			else if(perunBean instanceof User) user = (User) perunBean;
			else if(perunBean instanceof Host) host = (Host) perunBean;
			else if(perunBean instanceof Resource) resource = (Resource) perunBean;
			else {
				throw new InternalErrorException("There is unrecognized object in primaryHolder of aidingAttr.");
			}
		} else {
			throw new InternalErrorException("Aiding attribtue must have primaryHolder which is not null.");
		}

		//Important For Groups not work with Subgroups! Invalid members are executed too.

		if(group != null) {
			vos.add(getPerunBl().getVosManagerBl().getVoById(sess, group.getVoId()));
		} else if(member != null) {
			vos.add(getPerunBl().getMembersManagerBl().getMemberVo(sess, member));
		} else if(resource != null) {
			vos.add(getPerunBl().getVosManagerBl().getVoById(sess, resource.getVoId()));
		} else if(user != null) {
			vos.addAll(getPerunBl().getUsersManagerBl().getVosWhereUserIsMember(sess, user));
		} else if(host != null) {
			facility = getPerunBl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
			vos.addAll(getPerunBl().getFacilitiesManagerBl().getAllowedVos(sess, facility));
		} else if(facility != null) {
			vos.addAll(getPerunBl().getFacilitiesManagerBl().getAllowedVos(sess, facility));
		} else if(vo != null) {
			vos.add(vo);
		}

		vos = new ArrayList<Vo>(new HashSet<Vo>(vos));
		return vos;
	}

	/**
	 * Gets the vosManagerImpl.
	 *
	 * @return The vosManagerImpl.
	 */
	public VosManagerImplApi getVosManagerImpl() {
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
